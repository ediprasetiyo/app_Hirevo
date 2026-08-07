package com.hirevo.audit;

import com.hirevo.security.jwt.HirevoPrincipal;
import com.hirevo.tenant.TenantContext;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Intercepts {@link Audited}-annotated methods, resolves entity id via SpEL,
 * and publishes an {@link AuditEvent} to Kafka (fire-and-forget).
 */
@Aspect
@Component
public class AuditedAspect {

  private static final Logger log = LoggerFactory.getLogger(AuditedAspect.class);

  private final ExpressionParser parser = new SpelExpressionParser();
  private final AuditEventPublisher publisher;
  private final String serviceName;

  public AuditedAspect(
      AuditEventPublisher publisher,
      @Value("${spring.application.name:unknown-service}") String serviceName) {
    this.publisher = publisher;
    this.serviceName = serviceName;
  }

  @Around("@annotation(audited)")
  public Object around(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
    Object result = pjp.proceed();
    try {
      String entityId = resolveEntityId(pjp, audited, result);
      HirevoPrincipal principal = currentPrincipal();
      UUID tenantId = TenantContext.get().orElse(null);
      AuditEvent event = new AuditEvent(
          UUID.randomUUID().toString(),
          Instant.now(),
          tenantId,
          principal == null ? null : principal.userId(),
          serviceName,
          audited.module(),
          audited.action(),
          audited.entityType(),
          entityId,
          Map.of());
      publisher.publish(event);
    } catch (Exception ex) {
      // Audit failures never break business flow — log & continue.
      log.warn("Failed to publish audit event for {}.{}", audited.module(), audited.action(), ex);
    }
    return result;
  }

  private String resolveEntityId(ProceedingJoinPoint pjp, Audited audited, Object result) {
    if (audited.entityIdExpression().isBlank()) return null;
    MethodSignature sig = (MethodSignature) pjp.getSignature();
    String[] paramNames = sig.getParameterNames();
    Object[] args = pjp.getArgs();
    EvaluationContext ctx = new StandardEvaluationContext();
    for (int i = 0; i < paramNames.length; i++) {
      ((StandardEvaluationContext) ctx).setVariable(paramNames[i], args[i]);
    }
    ((StandardEvaluationContext) ctx).setVariable("result", result);
    Object v = parser.parseExpression(audited.entityIdExpression()).getValue(ctx);
    return v == null ? null : v.toString();
  }

  private static HirevoPrincipal currentPrincipal() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof HirevoPrincipal p) return p;
    return null;
  }

  Map<String, Object> emptyMap() { return new HashMap<>(); }
}
