package com.metricsmonitoring.metricsconsumer;

import java.time.Instant;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MetricEventListener {

    private static final Logger logger = LoggerFactory.getLogger(MetricEventListener.class);

    private final InfluxDBClient influxDBClient;

    public MetricEventListener(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    @KafkaListener(topics = "metrics")
    public void receive(MetricEvent event) {
        Instant timestamp = parseTimestamp(event.timestamp());

        Point point = Point.measurement("metrics")
                .addTag("metricName", tagValue(event.metricName()))
                .addTag("source", tagValue(event.source()))
                .addTag("instance", tagValue(event.instance()))
                .addField("value", event.value())
                .time(timestamp.toEpochMilli(), WritePrecision.MS);

        influxDBClient.getWriteApiBlocking().writePoint(point);
        logger.info("Stored Kafka metric in InfluxDB: metricName={}, value={}, source={}, instance={}, timestamp={}",
                event.metricName(), event.value(), event.source(), event.instance(), timestamp);
    }

    private static Instant parseTimestamp(String timestamp) {
        if (timestamp != null && !timestamp.isBlank()) {
            try {
                return Instant.parse(timestamp);
            } catch (RuntimeException exception) {
                logger.warn("Invalid metric timestamp '{}'; using current time", timestamp);
            }
        }
        return Instant.now();
    }

    private static String tagValue(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}