package com.hirevo.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Wire format for audit events published on the {@code hirevo.audit} Kafka topic. */
public record AuditEvent(
    String id,
    Instant occurredAt,
    UUID tenantId,
    UUID userId,
    String service,
    String module,
    String action,
    String entityType,
    String entityId,
    Map<String, Object> metadata) {}
