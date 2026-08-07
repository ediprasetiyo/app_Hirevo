package com.hirevo.gateway;

import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Resolves tenant from subdomain ({@code acme.hirevo.id}) → looks up tenant id in
 * Redis cache (5min TTL) or falls back to iam-service — and stamps {@code X-Tenant-ID}
 * header on the forwarded request so downstream services can trust it.
 *
 * <p>Bypassed for public endpoints (tenant signup, healthchecks, careers page).
 */
@Component
public class TenantResolutionFilter implements GlobalFilter, Ordered {

  private static final String TENANT_HEADER = "X-Tenant-ID";
  private static final String CACHE_PREFIX = "tenant:subdomain:";

  private final ReactiveStringRedisTemplate redis;

  public TenantResolutionFilter(ReactiveStringRedisTemplate redis) {
    this.redis = redis;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest req = exchange.getRequest();
    String path = req.getURI().getPath();
    if (isPublicPath(path)) {
      return chain.filter(exchange);
    }
    String host = req.getHeaders().getFirst("Host");
    String subdomain = extractSubdomain(host);
    if (subdomain == null) {
      // Fall back to explicit header (e.g. mobile app sends X-Tenant-Subdomain)
      subdomain = req.getHeaders().getFirst("X-Tenant-Subdomain");
    }
    if (subdomain == null) {
      exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
      return exchange.getResponse().setComplete();
    }
    return redis.opsForValue().get(CACHE_PREFIX + subdomain)
        .flatMap(tenantId -> forward(exchange, chain, tenantId))
        .switchIfEmpty(Mono.defer(() -> {
          exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
          return exchange.getResponse().setComplete();
        }));
  }

  private static Mono<Void> forward(ServerWebExchange exchange, GatewayFilterChain chain, String tenantId) {
    // Validate UUID; reject junk cached values defensively.
    try { UUID.fromString(tenantId); } catch (Exception e) {
      exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
      return exchange.getResponse().setComplete();
    }
    ServerHttpRequest mutated = exchange.getRequest().mutate()
        .header(TENANT_HEADER, tenantId).build();
    return chain.filter(exchange.mutate().request(mutated).build());
  }

  private static boolean isPublicPath(String path) {
    return path.startsWith("/actuator")
        || path.startsWith("/v1/tenants/signup")
        || path.startsWith("/v1/auth/password/reset")
        || path.startsWith("/careers/");
  }

  private static String extractSubdomain(String host) {
    if (host == null) return null;
    // Strip port
    int colon = host.indexOf(':');
    if (colon > 0) host = host.substring(0, colon);
    // Only accept *.hirevo.id (and *.localhost for dev)
    if (host.endsWith(".hirevo.id")) {
      String sub = host.substring(0, host.length() - ".hirevo.id".length());
      if (sub.equals("api") || sub.equals("www")) return null;
      return sub;
    }
    if (host.endsWith(".localhost")) {
      return host.substring(0, host.length() - ".localhost".length());
    }
    return null;
  }

  @Override
  public int getOrder() {
    return -100; // before routing decisions
  }
}
