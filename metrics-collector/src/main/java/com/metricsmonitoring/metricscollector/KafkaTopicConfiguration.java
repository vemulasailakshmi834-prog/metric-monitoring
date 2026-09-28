package com.metricsmonitoring.metricscollector;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic metricsTopic() {
        return TopicBuilder.name("metrics")
                .partitions(3)
                .replicas(1)
                .build();
    }
}