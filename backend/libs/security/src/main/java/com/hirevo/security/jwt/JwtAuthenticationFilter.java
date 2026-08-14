package com.hirevo.security.jwt;

import com.hirevo.core.exception.HirevoException;
import com.hirevo.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates the {@code Authorization: Bearer <jwt>} header, populates
 * {@link SecurityContextHolder} with a Hirevo principal + authorities,
 * and (if not already set) puts the tenant id into {@link TenantContext}.
 *
 * <p>On a missing/expired/invalid token this filter does NOT write a 401
 * response itself — it just leaves the request unauthenticated and lets the
 * chain continue. The authorization decision (permitAll vs authenticated)
 * belongs to Spring Security's {@code authorizeHttpRequests} rules, which run
 * after this filter. Short-circuiting here used to reject public endpoints
 * like {@code /v1/auth/login} whenever the browser happened to still be
 * carrying a stale/expired token from a previous session — the frontend
 * always attaches whatever token it has in storage, even to the login call.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTH_HEADER = "Authorization";
  private static final String BEARER = "Bearer ";

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader(AUTH_HEADER);
    if (header != null && header.startsWith(BEARER)) {
      String token = header.substring(BEARER.length());
      try {
        JwtService.ParsedClaims claims = jwtService.parse(token);
        List<SimpleGrantedAuthority> authorities =
            java.util.stream.Stream.concat(
                    claims.roles().stream().map(r -> "ROLE_" + r.toUpperCase()),
                    claims.permissions().stream())
                .map(SimpleGrantedAuthority::new)
                .toList();

        HirevoPrincipal principal = new HirevoPrincipal(claims);
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Only set tenant context if not already set by upstream (gateway forwarded header)
        if (TenantContext.get().isEmpty()) {
          TenantContext.set(claims.tenantId());
        }
      } catch (HirevoException ex) {
        // Invalid/expired token: proceed unauthenticated rather than rejecting
        // outright. Protected endpoints will still 401 via Spring Security's
        // own entry point; permitAll endpoints (login, refresh, signup,
        // swagger, actuator) work regardless of what stale token the client
        // happened to send.
        SecurityContextHolder.clearContext();
      }
    }
    chain.doFilter(request, response);
  }
}
