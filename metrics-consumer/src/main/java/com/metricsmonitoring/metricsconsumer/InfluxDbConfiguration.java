package com.metricsmonitoring.metricsconsumer;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDbConfiguration {

    @Bean(destroyMethod = "close")
    public InfluxDBClient influxDBClient(
            @Value("${influxdb.url}") String url,
            @Value("${influxdb.token}") String token,
            @Value("${influxdb.org}") String organization,
            @Value("${influxdb.bucket}") String bucket) {
        return InfluxDBClientFactory.create(url, token.toCharArray(), organization, bucket);
    }
}