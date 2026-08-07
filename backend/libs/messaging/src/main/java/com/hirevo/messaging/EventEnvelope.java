package com.hirevo.messaging;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard Kafka message envelope. Payload is the domain-specific event body.
 */
public record EventEnvelope<T>(
    String eventId,
    String eventType,
    int schemaVersion,
    Instant occurredAt,
    UUID tenantId,
    String traceId,
    T payload) {

  public static <T> EventEnvelope<T> of(String eventType, UUID tenantId, T payload) {
    return new EventEnvelope<>(
        UUID.randomUUID().toString(),
        eventType,
        1,
        Instant.now(),
        tenantId,
        null,
        payload);
  }
}
