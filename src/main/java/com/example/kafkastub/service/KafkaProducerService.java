package com.example.kafkastub.service;

import com.example.kafkastub.config.KafkaSecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<byte[], byte[]> kafkaTemplate;
    private final KafkaSecurityProperties kafkaSecurityProperties;

    public KafkaProducerService(KafkaTemplate<byte[], byte[]> kafkaTemplate, KafkaSecurityProperties kafkaSecurityProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaSecurityProperties = kafkaSecurityProperties;
    }

    public void send(String topic, byte[] payload) {
        applyRateLimit();

        try {
            CompletableFuture<SendResult<byte[], byte[]>> future = kafkaTemplate.send(topic, payload);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Kafka send failed for topic {}", topic, ex);
                } else {
                    log.info("Message sent to topic {}, partition={}, offset={}",
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("Unexpected producer error while sending message to topic {}", topic, e);
        }
    }

    private void applyRateLimit() {
        long delayMs = kafkaSecurityProperties.getRateLimitMs();
        if (delayMs <= 0) {
            return;
        }

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Producer rate-limit sleep interrupted");
        }
    }
}
