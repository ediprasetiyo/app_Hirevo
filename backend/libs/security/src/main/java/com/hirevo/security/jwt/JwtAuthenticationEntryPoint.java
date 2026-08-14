package com.hirevo.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Without an explicit {@link AuthenticationEntryPoint} bean, Spring Security
 * falls back to {@code Http403ForbiddenEntryPoint} for any request that
 * reaches an {@code .anyRequest().authenticated()} rule without a valid
 * principal — including the common case of an expired/missing JWT. That
 * reads to a client as "forbidden" (403) rather than "please log in again"
 * (401), which matters here because the frontend's token-refresh interceptor
 * only retries on 401. This bean restores the conventional 401 for "not
 * authenticated at all", leaving 403 for "authenticated but lacking a
 * specific permission" (still handled by the default AccessDeniedHandler).
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/problem+json");
    response.getWriter().write(
        "{\"type\":\"about:blank\",\"title\":\"Unauthenticated\",\"status\":401,"
            + "\"detail\":\"Access token missing, invalid, or expired\",\"code\":\"auth.token_expired\"}");
  }
}
