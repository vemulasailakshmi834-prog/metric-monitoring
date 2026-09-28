package com.metricsmonitoring.sampleapp;

import java.time.Duration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Custom application metrics. Micrometer uses dotted names in Java.
 * Prometheus then publishes them as:
 * sample_work_requests_total, sample_error_requests_total,
 * sample_slow_request_duration_seconds.
 */
@Component
public class SampleMetrics {

    private final Counter workRequests;
    private final Counter errorRequests;
    private final Timer slowRequestTimer;

    public SampleMetrics(MeterRegistry meterRegistry) {
        this.workRequests = Counter.builder("sample.work.requests")
                .description("Number of /work requests")
                .register(meterRegistry);
        this.errorRequests = Counter.builder("sample.error.requests")
                .description("Number of intentional /error requests")
                .register(meterRegistry);
        this.slowRequestTimer = Timer.builder("sample.slow.request.duration")
                .description("Time spent handling /slow requests")
                .register(meterRegistry);
    }

    public void countWorkRequest() {
        workRequests.increment();
    }

    public void countErrorRequest() {
        errorRequests.increment();
    }

    public void recordSlow(Duration duration) {
        slowRequestTimer.record(duration);
    }
}
