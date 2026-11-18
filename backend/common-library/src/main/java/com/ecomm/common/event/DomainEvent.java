package com.ecomm.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard enterprise event envelope conforming to e-comm architecture blueprint.
 *
 * @param <T> Event payload type
 */
public class DomainEvent<T> {

    private String eventId;
    private String eventType;
    private int eventVersion;
    private Instant occurredAt;
    private String producer;
    private String correlationId;
    private String aggregateType;
    private String aggregateId;
    private long aggregateVersion;
    private T payload;

    public DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.eventVersion = 1;
        this.occurredAt = Instant.now();
    }

    public DomainEvent(String eventId, String eventType, int eventVersion, Instant occurredAt,
                       String producer, String correlationId, String aggregateType,
                       String aggregateId, long aggregateVersion, T payload) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
        this.eventType = eventType;
        this.eventVersion = eventVersion > 0 ? eventVersion : 1;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.producer = producer;
        this.correlationId = correlationId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.payload = payload;
    }

    public static <T> DomainEvent<T> create(String eventType, String producer, String aggregateType,
                                           String aggregateId, long aggregateVersion, T payload) {
        return new DomainEvent<>(
                UUID.randomUUID().toString(),
                eventType,
                1,
                Instant.now(),
                producer,
                UUID.randomUUID().toString(),
                aggregateType,
                aggregateId,
                aggregateVersion,
                payload
        );
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public int getEventVersion() { return eventVersion; }
    public void setEventVersion(int eventVersion) { this.eventVersion = eventVersion; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public String getProducer() { return producer; }
    public void setProducer(String producer) { this.producer = producer; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }

    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }

    public long getAggregateVersion() { return aggregateVersion; }
    public void setAggregateVersion(long aggregateVersion) { this.aggregateVersion = aggregateVersion; }

    public T getPayload() { return payload; }
    public void setPayload(T payload) { this.payload = payload; }
}
