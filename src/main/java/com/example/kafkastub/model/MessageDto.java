package com.example.kafkastub.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonPropertyOrder({"id", "type", "payload"})
public class MessageDto {

    private String id;
    private String type;
    private Map<String, Object> payload = new LinkedHashMap<>();

    public MessageDto() {
    }

    public MessageDto(String id, String type, Map<String, Object> payload) {
        this.id = id;
        this.type = type;
        setPayload(payload);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload == null ? new LinkedHashMap<>() : new LinkedHashMap<>(payload);
    }

    @Override
    public String toString() {
        return "MessageDto{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", payload=" + payload +
                '}';
    }
}
