package com.hirevo.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes audit events to Kafka topic {@code hirevo.audit.v1}.
 * Consumers: audit-service (persists + hashes), reporting-service, SIEM.
 */
@Component
public class AuditEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(AuditEventPublisher.class);
  public static final String TOPIC = "hirevo.audit.v1";

  private final KafkaTemplate<String, AuditEvent> kafka;

  @Autowired(required = false)
  public AuditEventPublisher(KafkaTemplate<String, AuditEvent> kafka) {
    this.kafka = kafka;
  }

  public void publish(AuditEvent event) {
    if (kafka == null) {
      log.debug("Kafka not configured — audit event dropped: {}", event);
      return;
    }
    String key = event.tenantId() == null ? "system" : event.tenantId().toString();
    kafka.send(TOPIC, key, event);
  }
}
