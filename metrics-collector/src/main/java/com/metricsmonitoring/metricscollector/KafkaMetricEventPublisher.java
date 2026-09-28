package com.metricsmonitoring.metricscollector;

import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaMetricEventPublisher implements MetricEventPublisher {

    private final KafkaTemplate<String, MetricEvent> kafkaTemplate;

    public KafkaMetricEventPublisher(KafkaTemplate<String, MetricEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, MetricEvent event) throws Exception {
        kafkaTemplate.send(topic, event).get(10, TimeUnit.SECONDS);
    }
}