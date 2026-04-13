package com.example.kafkastub.util;

import com.example.kafkastub.model.MessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Маппер для нестабильного входного формата (object/array).
 *
 * <p>Поддерживаем 2 варианта:
 * 1) object: {"id":"...","type":"...","payload":{...}}
 * 2) array:  ["id","type",{...}]
 *
 * <p>Зачем это вынесено отдельно:
 * - listener/service не знают о деталях парсинга;
 * - проще расширять (например добавить v2 схемы).
 */
@Component
public class FlexibleMessageMapper {

    private final ObjectMapper objectMapper;

    public FlexibleMessageMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public MessageDto fromJsonNode(JsonNode rootNode) {
        if (rootNode == null || rootNode.isNull()) {
            return new MessageDto();
        }

        if (rootNode.isArray()) {
            String id = rootNode.path(0).asText(null);
            String type = rootNode.path(1).asText(null);
            Map<String, Object> payload = toPayload(rootNode.path(2));
            return new MessageDto(id, type, payload);
        }

        if (rootNode.isObject()) {
            String id = rootNode.path("id").asText(null);
            String type = rootNode.path("type").asText(null);
            Map<String, Object> payload = toPayload(rootNode.path("payload"));
            return new MessageDto(id, type, payload);
        }

        // Фолбэк для поврежденных/неожиданных форматов.
        return new MessageDto(null, "UNKNOWN", Map.of("raw", rootNode.asText()));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toPayload(JsonNode payloadNode) {
        if (payloadNode == null || payloadNode.isNull() || payloadNode.isMissingNode()) {
            return new LinkedHashMap<>();
        }

        if (payloadNode.isObject()) {
            return objectMapper.convertValue(payloadNode, Map.class);
        }

        if (payloadNode.isArray()) {
            return Map.of("items", objectMapper.convertValue(payloadNode, Object.class));
        }

        return Map.of("value", payloadNode.asText());
    }

    public MessageDto fromJsonBytes(byte[] jsonBytes) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(jsonBytes);
        return fromJsonNode(node);
    }
}
