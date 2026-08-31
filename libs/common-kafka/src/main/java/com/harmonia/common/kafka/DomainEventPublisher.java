package com.harmonia.common.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    public DomainEventPublisher(KafkaTemplate<String, DomainEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, DomainEvent event) {
        kafkaTemplate.send(topic, event.aggregateId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} to {}", event.eventType(), topic, ex);
                    } else {
                        log.debug("Published {} to {} partition {}", event.eventType(), topic,
                                result.getRecordMetadata().partition());
                    }
                });
    }
}
