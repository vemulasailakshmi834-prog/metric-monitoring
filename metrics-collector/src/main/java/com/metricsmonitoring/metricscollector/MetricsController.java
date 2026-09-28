package com.metricsmonitoring.metricscollector;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController {

    private static final String TOPIC = "metrics";

    private final MetricEventPublisher metricEventPublisher;

    public MetricsController(MetricEventPublisher metricEventPublisher) {
        this.metricEventPublisher = metricEventPublisher;
    }

    @PostMapping("/metrics")
    public ResponseEntity<String> publish(@RequestBody MetricEvent event) throws Exception {
        metricEventPublisher.publish(TOPIC, event);
        return ResponseEntity.ok("Metric event published to Kafka topic: " + TOPIC);
    }
}