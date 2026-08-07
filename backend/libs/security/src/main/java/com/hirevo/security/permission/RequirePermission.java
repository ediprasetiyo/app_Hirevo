package com.hirevo.security.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Convenience annotation — expands to Spring Security's {@code @PreAuthorize}
 * with a hasPermission check. Example: {@code @RequirePermission("payroll.run")}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(#root.args[0])")
public @interface RequirePermission {
  String value();
}
