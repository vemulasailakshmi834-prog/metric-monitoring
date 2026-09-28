package com.metricsmonitoring.metricsconsumer;

public record MetricEvent(
        String metricName,
        double value,
        String timestamp,
        String source,
        String instance) {
}