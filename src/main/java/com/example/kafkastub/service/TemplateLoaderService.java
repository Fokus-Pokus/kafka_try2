package com.example.kafkastub.service;

import com.example.kafkastub.config.TemplateProperties;
import com.example.kafkastub.model.MessageDto;
import com.example.kafkastub.serializer.MessagePackService;
import com.example.kafkastub.util.FlexibleMessageMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Загружает шаблоны из файлов при старте приложения и держит их в памяти в двух видах:
 * 1) MessageDto — удобно модифицировать в бизнес-логике,
 * 2) byte[] MessagePack — готовый бинарник для отправки/отладки.
 *
 * <p>Почему отдельно от listener/service:
 * - изоляция I/O логики и формата шаблонов;
 * - переиспользование в нескольких потоках Kafka;
 * - упрощение тестирования.
 *
 * <p>Документация:
 * - Spring Resource abstraction: https://docs.spring.io/spring-framework/reference/core/resources.html
 * - Jackson YAML: https://github.com/FasterXML/jackson-dataformats-text/tree/2.x/yaml
 */
@Service
public class TemplateLoaderService {

    private static final Logger log = LoggerFactory.getLogger(TemplateLoaderService.class);

    private final TemplateProperties templateProperties;
    private final FlexibleMessageMapper flexibleMessageMapper;
    private final MessagePackService messagePackService;
    private final ResourceLoader resourceLoader;

    /**
     * Используем отдельный YAML ObjectMapper, чтобы читать шаблоны как "вложенный список" (YAML list),
     * а не только чистый JSON.
     */
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    /** Кэш DTO-шаблонов по имени файла без расширения. */
    private final Map<String, MessageDto> templates = new ConcurrentHashMap<>();

    /** Кэш уже сериализованных MessagePack-байтов для тех же шаблонов. */
    private final Map<String, byte[]> templateBinaries = new ConcurrentHashMap<>();

    public TemplateLoaderService(
            TemplateProperties templateProperties,
            FlexibleMessageMapper flexibleMessageMapper,
            MessagePackService messagePackService,
            ResourceLoader resourceLoader
    ) {
        this.templateProperties = templateProperties;
        this.flexibleMessageMapper = flexibleMessageMapper;
        this.messagePackService = messagePackService;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Загружаем шаблоны ровно из списка app.templates.files.
     * Это защищает от случайного подхвата лишних файлов из каталога.
     */
    @PostConstruct
    public void loadTemplates() {
        for (String fileName : templateProperties.getFiles()) {
            String resourcePath = "classpath:" + templateProperties.getPath() + "/" + fileName;
            Resource resource = resourceLoader.getResource(resourcePath);
            loadSingleTemplate(resource, fileName);
        }

        log.info("Loaded {} template DTO(s) and {} MessagePack binary template(s)",
                templates.size(), templateBinaries.size());
    }

    public MessageDto getTemplate(String key) {
        MessageDto dto = templates.get(key);
        if (dto == null) {
            return null;
        }
        // Возвращаем копию, чтобы бизнес-логика не портила оригинал в кэше.
        return new MessageDto(dto.getId(), dto.getType(), dto.getPayload());
    }

    public byte[] getTemplateBinary(String key) {
        byte[] bytes = templateBinaries.get(key);
        return bytes == null ? null : bytes.clone();
    }

    public Map<String, MessageDto> getAllTemplates() {
        return Collections.unmodifiableMap(templates);
    }

    private void loadSingleTemplate(Resource resource, String fileName) {
        String key = fileName.replaceFirst("\\.[^.]+$", "");

        if (!resource.exists()) {
            log.error("Template file is not found: {}", fileName);
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode root = yamlMapper.readTree(inputStream);

            // Поддержка "вложенного списка":
            // 1) [id, type, payload]
            // 2) [[id, type, payload]]
            // 3) object JSON/YAML {id, type, payload}
            JsonNode normalized = normalizeNestedListIfNeeded(root);

            MessageDto dto = flexibleMessageMapper.fromJsonNode(normalized);
            templates.put(key, dto);
            templateBinaries.put(key, safeToMessagePack(dto, key));
            log.info("Template loaded: {}", key);
        } catch (IOException e) {
            log.error("Failed to parse template {}", fileName, e);
        }
    }

    private JsonNode normalizeNestedListIfNeeded(JsonNode root) {
        if (root != null && root.isArray() && root.size() == 1 && root.get(0).isArray()) {
            return root.get(0);
        }
        return root;
    }

    private byte[] safeToMessagePack(MessageDto dto, String key) {
        try {
            return messagePackService.serialize(dto);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize template {} into MessagePack binary", key, e);
            return new byte[0];
        }
    }
}
