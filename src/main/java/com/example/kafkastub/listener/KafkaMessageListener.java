package com.example.kafkastub.listener;

import com.example.kafkastub.config.KafkaTopicsProperties;
import com.example.kafkastub.service.MessageFlowService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageListener {

    private final MessageFlowService messageFlowService;
    private final KafkaTopicsProperties topicsProperties;

    public KafkaMessageListener(MessageFlowService messageFlowService, KafkaTopicsProperties topicsProperties) {
        this.messageFlowService = messageFlowService;
        this.topicsProperties = topicsProperties;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.topic1-input}",
            containerFactory = "kafkaListenerContainerFactoryGroup1"
    )
    public void listenTopic1(byte[] message) {
        messageFlowService.handleIncoming(message, topicsProperties.getTopic2Output(), "default-response", "FLOW-1");
    }

    @KafkaListener(
            topics = "${app.kafka.topics.topic3-input}",
            containerFactory = "kafkaListenerContainerFactoryGroup2"
    )
    public void listenTopic3(byte[] message) {
        messageFlowService.handleIncoming(message, topicsProperties.getTopic4Output(), "default-response", "FLOW-2");
    }
}
