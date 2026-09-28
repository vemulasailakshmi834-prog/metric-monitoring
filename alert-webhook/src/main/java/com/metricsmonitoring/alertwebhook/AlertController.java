package com.metricsmonitoring.alertwebhook;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlertController {

    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);

    @PostMapping("/alerts")
    public ResponseEntity<Void> receiveAlert(@RequestBody JsonNode payload) {
        String status = text(payload, "status");
        JsonNode alerts = payload.path("alerts");

        for (JsonNode alert : alerts) {
            JsonNode labels = alert.path("labels");
            JsonNode annotations = alert.path("annotations");
            logger.info(
                    "Alertmanager notification: status={}, alertname={}, severity={}, instance={}, summary={}, description={}",
                    status,
                    text(labels, "alertname"),
                    text(labels, "severity"),
                    text(labels, "instance"),
                    text(annotations, "summary"),
                    text(annotations, "description"));
        }

        return ResponseEntity.accepted().build();
    }

    private static String text(JsonNode node, String field) {
        return node.path(field).asText("unknown");
    }
}