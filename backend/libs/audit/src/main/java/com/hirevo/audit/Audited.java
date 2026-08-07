package com.hirevo.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method whose invocation should emit an audit event.
 * Handled by {@link AuditedAspect}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

  /** Module the action belongs to (e.g. {@code "payroll"}). */
  String module();

  /** Action verb (e.g. {@code "run"}, {@code "approve"}, {@code "login"}). */
  String action();

  /** Entity type acted upon (e.g. {@code "payroll_run"}). */
  String entityType() default "";

  /**
   * SpEL expression to extract the entity id from method args, e.g.
   * {@code "#payrollRunId"} or {@code "#result.id"}.
   */
  String entityIdExpression() default "";
}
