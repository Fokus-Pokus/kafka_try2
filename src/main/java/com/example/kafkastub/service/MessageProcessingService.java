package com.example.kafkastub.service;

import com.example.kafkastub.model.MessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Бизнес-логика формирования ответа.
 *
 * <p>Сначала берем шаблон (если есть), затем накладываем значения из входящего сообщения.
 * Это имитирует "продовый" подход, где шаблон задает базовую форму ответа.
 */
@Service
public class MessageProcessingService {

    private static final Logger log = LoggerFactory.getLogger(MessageProcessingService.class);

    private final TemplateLoaderService templateLoaderService;

    public MessageProcessingService(TemplateLoaderService templateLoaderService) {
        this.templateLoaderService = templateLoaderService;
    }

    public MessageDto buildResponse(MessageDto incoming, String templateKey) {
        MessageDto template = templateLoaderService.getTemplate(templateKey);

        MessageDto response = template != null ? template : new MessageDto();
        response.setId(incoming.getId() != null ? incoming.getId() : UUID.randomUUID().toString());
        response.setType(incoming.getType() != null ? incoming.getType() + "_PROCESSED" : "PROCESSED");

        Map<String, Object> payload = new LinkedHashMap<>(response.getPayload());
        payload.put("sourcePayload", incoming.getPayload());
        payload.put("processedAt", Instant.now().toString());
        response.setPayload(payload);

        log.info("Built response message: {}", response);
        return response;
    }
}
