package com.ecomm.product.kafka;

import com.ecomm.common.event.DomainEvent;
import com.ecomm.product.dto.ProductResponse;
import com.ecomm.product.entity.OutboxEvent;
import com.ecomm.product.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class ProductEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.product-events:product.events.v1}")
    private String productEventsTopic;

    public ProductEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                 OutboxEventRepository outboxEventRepository,
                                 ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void saveToOutbox(String eventType, ProductResponse product) {
        DomainEvent<ProductResponse> event = DomainEvent.create(
                eventType,
                "product-service",
                "Product",
                product.getId(),
                product.getVersion() != null ? product.getVersion() : 1L,
                product
        );

        try {
            String payloadJson = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("Product")
                    .aggregateId(product.getId())
                    .eventType(eventType)
                    .payload(payloadJson)
                    .status("PENDING")
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.info("Saved outbox event: {} for product: {}", eventType, product.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize domain event for product: {}", product.getId(), e);
        }
    }

    public void saveGenericToOutbox(String eventType, String aggregateType, String aggregateId, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payloadJson)
                    .status("PENDING")
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.info("Saved outbox event: {} for {}: {}", eventType, aggregateType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize domain event for {}: {}", aggregateId, e);
        }
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(productEventsTopic, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                log.info("Successfully published Kafka event: {} for aggregate: {}", event.getEventType(), event.getAggregateId());
                            } else {
                                log.warn("Kafka publish error for event {}: {}", event.getId(), ex.getMessage());
                            }
                        });

                event.setStatus("PUBLISHED");
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Error dispatching outbox event {}", event.getId(), e);
                event.setStatus("FAILED");
                outboxEventRepository.save(event);
            }
        }
    }
}
