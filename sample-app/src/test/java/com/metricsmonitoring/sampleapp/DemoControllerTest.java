package com.metricsmonitoring.sampleapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void workReturnsOk() {
        ResponseEntity<String> response = restTemplate.getForEntity("/work", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).startsWith("Work completed:");
    }

    @Test
    void slowReturnsOkAfterDelay() {
        ResponseEntity<String> response = restTemplate.getForEntity("/slow", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Slow work completed");
    }

    @Test
    void errorReturnsInternalServerError() {
        ResponseEntity<String> response = restTemplate.getForEntity("/error", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Simulated error");
    }

    @Test
    void prometheusEndpointExposesCustomMetrics() {
        restTemplate.getForEntity("/work", String.class);
        restTemplate.getForEntity("/error", String.class);
        restTemplate.getForEntity("/slow", String.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.TEXT_PLAIN));
        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/prometheus",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("sample_work_requests_total");
        assertThat(response.getBody()).contains("sample_error_requests_total");
        assertThat(response.getBody()).contains("sample_slow_request_duration");
    }
}
