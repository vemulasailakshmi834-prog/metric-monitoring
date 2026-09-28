package com.metricsmonitoring.metricscollector;

public record MetricEvent(
        String metricName,
        double value,
        String timestamp,
        String source,
        String instance) {
}