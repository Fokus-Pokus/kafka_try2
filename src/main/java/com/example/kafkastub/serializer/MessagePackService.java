package com.example.kafkastub.serializer;

import com.example.kafkastub.model.MessageDto;
import com.example.kafkastub.util.FlexibleMessageMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.springframework.stereotype.Service;

@Service
public class MessagePackService {

    private final ObjectMapper messagePackMapper;
    private final FlexibleMessageMapper flexibleMessageMapper;

    public MessagePackService(FlexibleMessageMapper flexibleMessageMapper) {
        this.messagePackMapper = new ObjectMapper(new MessagePackFactory());
        this.flexibleMessageMapper = flexibleMessageMapper;
    }

    public MessageDto deserialize(byte[] payloadBytes) throws JsonProcessingException {
        JsonNode rootNode = messagePackMapper.readTree(payloadBytes);
        return flexibleMessageMapper.fromJsonNode(rootNode);
    }

    public byte[] serialize(MessageDto dto) throws JsonProcessingException {
        return messagePackMapper.writeValueAsBytes(dto);
    }
}
