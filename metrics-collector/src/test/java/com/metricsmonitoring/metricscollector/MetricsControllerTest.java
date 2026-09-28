package com.metricsmonitoring.metricscollector;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetricsController.class)
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricEventPublisher metricEventPublisher;

    @Test
    void publishesMetricEventToMetricsTopic() throws Exception {
        mockMvc.perform(post("/metrics")
                        .contentType("application/json")
                        .content("""
                                {
                                  "metricName": "http_requests_total",
                                  "value": 1.0,
                                  "timestamp": "2026-09-28T10:00:00Z",
                                  "source": "sample-app",
                                  "instance": "sample-app-1"
                                }
                                """))
                .andExpect(status().isOk());

        verify(metricEventPublisher).publish(eq("metrics"), any(MetricEvent.class));
    }
}