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
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/problem+json");
        response.getWriter()
            .write("{\"type\":\"about:blank\",\"title\":\"" + ex.errorCode().code()
                + "\",\"status\":401,\"detail\":\"" + ex.getMessage() + "\"}");
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
