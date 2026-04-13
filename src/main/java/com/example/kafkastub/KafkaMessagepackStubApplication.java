package com.example.kafkastub;

import com.example.kafkastub.config.KafkaSecurityProperties;
import com.example.kafkastub.config.KafkaTopicsProperties;
import com.example.kafkastub.config.TemplateProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
@EnableConfigurationProperties({KafkaTopicsProperties.class, KafkaSecurityProperties.class, TemplateProperties.class})
public class KafkaMessagepackStubApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaMessagepackStubApplication.class, args);
    }
}
