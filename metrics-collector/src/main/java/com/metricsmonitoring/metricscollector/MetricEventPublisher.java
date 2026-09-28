package com.metricsmonitoring.metricscollector;

public interface MetricEventPublisher {

    void publish(String topic, MetricEvent event) throws Exception;
}