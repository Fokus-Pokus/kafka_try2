package com.example.kafkastub.service;

import com.example.kafkastub.config.TemplateProperties;
import com.example.kafkastub.model.MessageDto;
import com.example.kafkastub.util.FlexibleMessageMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TemplateLoaderService {

    private static final Logger log = LoggerFactory.getLogger(TemplateLoaderService.class);

    private final TemplateProperties templateProperties;
    private final ObjectMapper objectMapper;
    private final FlexibleMessageMapper flexibleMessageMapper;
    private final Map<String, MessageDto> templates = new ConcurrentHashMap<>();

    public TemplateLoaderService(
            TemplateProperties templateProperties,
            ObjectMapper objectMapper,
            FlexibleMessageMapper flexibleMessageMapper
    ) {
        this.templateProperties = templateProperties;
        this.objectMapper = objectMapper;
        this.flexibleMessageMapper = flexibleMessageMapper;
    }

    @PostConstruct
    public void loadTemplates() {
        String pattern = "classpath*:" + templateProperties.getPath() + "/*.json";
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            Resource[] resources = resolver.getResources(pattern);
            for (Resource resource : resources) {
                loadSingleTemplate(resource);
            }
            log.info("Loaded {} template(s) from {}", templates.size(), templateProperties.getPath());
        } catch (IOException e) {
            log.error("Failed to load templates from {}", templateProperties.getPath(), e);
        }
    }

    public MessageDto getTemplate(String key) {
        MessageDto dto = templates.get(key);
        if (dto == null) {
            return null;
        }
        return new MessageDto(dto.getId(), dto.getType(), dto.getPayload());
    }

    public Map<String, MessageDto> getAllTemplates() {
        return Collections.unmodifiableMap(templates);
    }

    private void loadSingleTemplate(Resource resource) {
        String fileName = resource.getFilename();
        if (fileName == null) {
            return;
        }

        String key = fileName.replaceFirst("\\.json$", "");

        try (InputStream inputStream = resource.getInputStream()) {
            MessageDto dto = flexibleMessageMapper.fromJsonNode(objectMapper.readTree(inputStream));
            templates.put(key, dto);
            log.info("Template loaded: {}", key);
        } catch (IOException e) {
            log.error("Failed to parse template {}", fileName, e);
        }
    }
}
