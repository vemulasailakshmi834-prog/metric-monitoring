package com.metricsmonitoring.metricsconsumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConfiguration.class);

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(this::logFailedRecord, new FixedBackOff(1_000L, 3L));
    }

    private void logFailedRecord(ConsumerRecord<?, ?> record, Exception exception) {
        logger.error("Unable to process Kafka record after retries: topic={}, partition={}, offset={}, value={}",
                record.topic(), record.partition(), record.offset(), record.value(), exception);
    }
}