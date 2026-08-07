package com.hirevo.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Extracts tenant id from the {@code X-Tenant-ID} header (set by API Gateway)
 * and stores it in {@link TenantContext} for the request lifetime.
 * Runs early — before Spring Security so security-context can read it if needed.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantContextFilter extends OncePerRequestFilter {

  public static final String HEADER = "X-Tenant-ID";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader(HEADER);
    try {
      if (header != null && !header.isBlank()) {
        TenantContext.set(UUID.fromString(header));
      }
      chain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri.startsWith("/actuator") || uri.startsWith("/v1/tenants/signup");
  }
}
