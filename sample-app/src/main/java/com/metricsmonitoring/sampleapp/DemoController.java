package com.metricsmonitoring.sampleapp;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    private final SampleMetrics sampleMetrics;

    public DemoController(SampleMetrics sampleMetrics) {
        this.sampleMetrics = sampleMetrics;
    }

    @GetMapping("/work")
    public String work() {
        int total = 0;
        for (int i = 1; i <= 1000; i++) {
            total += i;
        }
        sampleMetrics.countWorkRequest();
        return "Work completed: " + total;
    }

    @GetMapping("/slow")
    public String slow() {
        long started = System.nanoTime();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("The slow request was interrupted", ex);
        }
        sampleMetrics.recordSlow(Duration.ofNanos(System.nanoTime() - started));
        return "Slow work completed";
    }

    @GetMapping("/error")
    public ResponseEntity<String> error() {
        sampleMetrics.countErrorRequest();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Simulated error");
    }
}
