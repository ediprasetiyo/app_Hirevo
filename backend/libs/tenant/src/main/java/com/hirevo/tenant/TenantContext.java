package com.hirevo.tenant;

import java.util.Optional;
import java.util.UUID;

/**
 * ThreadLocal store of the resolved tenant for the current request/thread.
 * Populated by {@link TenantContextFilter}, cleared after the request completes.
 */
public final class TenantContext {

  private static final ThreadLocal<UUID> TENANT = new ThreadLocal<>();

  private TenantContext() {}

  public static void set(UUID tenantId) {
    TENANT.set(tenantId);
  }

  public static Optional<UUID> get() {
    return Optional.ofNullable(TENANT.get());
  }

  public static UUID getRequired() {
    UUID id = TENANT.get();
    if (id == null) {
      throw new IllegalStateException(
          "Tenant context missing — request likely bypassed TenantContextFilter");
    }
    return id;
  }

  public static void clear() {
    TENANT.remove();
  }
}
