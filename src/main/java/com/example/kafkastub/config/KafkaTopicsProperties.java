package com.example.kafkastub.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka.topics")
public class KafkaTopicsProperties {

    @NotBlank
    private String topic1Input;

    @NotBlank
    private String topic2Output;

    @NotBlank
    private String topic3Input;

    @NotBlank
    private String topic4Output;

    public String getTopic1Input() {
        return topic1Input;
    }

    public void setTopic1Input(String topic1Input) {
        this.topic1Input = topic1Input;
    }

    public String getTopic2Output() {
        return topic2Output;
    }

    public void setTopic2Output(String topic2Output) {
        this.topic2Output = topic2Output;
    }

    public String getTopic3Input() {
        return topic3Input;
    }

    public void setTopic3Input(String topic3Input) {
        this.topic3Input = topic3Input;
    }

    public String getTopic4Output() {
        return topic4Output;
    }

    public void setTopic4Output(String topic4Output) {
        this.topic4Output = topic4Output;
    }
}
