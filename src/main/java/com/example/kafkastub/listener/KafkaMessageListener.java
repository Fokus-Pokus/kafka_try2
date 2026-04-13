package com.example.kafkastub.listener;

import com.example.kafkastub.config.KafkaTopicsProperties;
import com.example.kafkastub.service.MessageFlowService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumers для двух независимых потоков.
 *
 * <p>Здесь только прием и делегирование в service-слой —
 * чтобы listener оставался тонким и легко тестировался.
 *
 * <p>Документация @KafkaListener:
 * https://docs.spring.io/spring-kafka/reference/kafka/receiving-messages/listener-annotation.html
 */
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
        messageFlowService.handleIncoming(message, topicsProperties.getTopic2Output(), "template-flow-1", "FLOW-1");
    }

    @KafkaListener(
            topics = "${app.kafka.topics.topic3-input}",
            containerFactory = "kafkaListenerContainerFactoryGroup2"
    )
    public void listenTopic3(byte[] message) {
        messageFlowService.handleIncoming(message, topicsProperties.getTopic4Output(), "template-flow-2", "FLOW-2");
    }
}
