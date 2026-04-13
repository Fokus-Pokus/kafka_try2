package com.example.kafkastub.service;

import com.example.kafkastub.model.MessageDto;
import com.example.kafkastub.serializer.MessagePackService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Оркестратор полного цикла обработки:
 * MessagePack вход -> DTO -> бизнес-обработка -> MessagePack выход -> отправка в Kafka.
 */
@Service
public class MessageFlowService {

    private static final Logger log = LoggerFactory.getLogger(MessageFlowService.class);

    private final MessagePackService messagePackService;
    private final MessageProcessingService messageProcessingService;
    private final KafkaProducerService kafkaProducerService;

    public MessageFlowService(
            MessagePackService messagePackService,
            MessageProcessingService messageProcessingService,
            KafkaProducerService kafkaProducerService
    ) {
        this.messagePackService = messagePackService;
        this.messageProcessingService = messageProcessingService;
        this.kafkaProducerService = kafkaProducerService;
    }

    public void handleIncoming(byte[] inputBytes, String outputTopic, String templateKey, String flowName) {
        try {
            MessageDto incoming = messagePackService.deserialize(inputBytes);
            log.info("{} incoming message: {}", flowName, incoming);

            MessageDto response = messageProcessingService.buildResponse(incoming, templateKey);
            byte[] responseBytes = messagePackService.serialize(response);
            kafkaProducerService.send(outputTopic, responseBytes);
        } catch (JsonProcessingException e) {
            // Важный контракт по задаче: не падать на невалидном MessagePack.
            log.error("{} failed to deserialize/serialize MessagePack payload", flowName, e);
        } catch (Exception e) {
            log.error("{} unexpected processing error", flowName, e);
        }
    }
}
