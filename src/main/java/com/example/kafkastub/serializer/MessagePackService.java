package com.example.kafkastub.serializer;

import com.example.kafkastub.model.MessageDto;
import com.example.kafkastub.util.FlexibleMessageMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.springframework.stereotype.Service;

/**
 * Сервис конвертации MessagePack <-> DTO.
 *
 * <p>Почему отдельный сервис:
 * - отделяем формат транспорта (MessagePack) от бизнес-логики;
 * - одна точка для возможной настройки ObjectMapper/модулей.
 *
 * <p>Документация MessagePack for Jackson:
 * https://github.com/msgpack/msgpack-java/tree/master/msgpack-jackson
 */
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
