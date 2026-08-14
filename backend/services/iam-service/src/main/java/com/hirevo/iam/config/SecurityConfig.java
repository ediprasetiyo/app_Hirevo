package com.hirevo.iam.config;

import com.hirevo.security.jwt.JwtAuthenticationFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  /**
   * Comma-separated origin patterns allowed to call this API from a browser.
   * Defaults cover local Next.js dev + the production wildcard subdomain
   * pattern (acme.hirevo.id, testco.hirevo.id, ...). In front of a real
   * gateway this still matters for direct-to-service local dev / Swagger UI.
   */
  @Value("${hirevo.security.cors.allowed-origin-patterns:http://localhost:3000,https://*.hirevo.id}")
  private String allowedOriginPatterns;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter)
      throws Exception {
    // Called directly (not autowired as a method parameter) because Spring MVC's
    // HandlerMappingIntrospector ALSO implements CorsConfigurationSource, and
    // autowiring-by-type would then see two candidate beans and fail to start.
    // Since this is the same @Configuration class, the CGLIB proxy still returns
    // the singleton bean instance rather than re-instantiating it.
    return http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**",
                             "/v1/auth/login", "/v1/auth/refresh", "/v1/auth/mfa/verify",
                             "/v1/auth/password/reset/**", "/v1/tenants/signup").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  /**
   * Without this bean, {@code .cors(cors -> {})} enables the CORS filter machinery
   * but leaves it with no actual configuration — Spring Security then denies
   * the browser's preflight OPTIONS request (observed as a 403 with no
   * Access-Control-Allow-Origin header), which the browser reports as a
   * generic "blocked by CORS policy" even though the real server never
   * rejected the request logically — it just never got past preflight.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of(allowedOriginPatterns.split(",")));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of(
        "Authorization", "Content-Type", "X-Tenant-Subdomain", "X-Tenant-ID",
        "Idempotency-Key", "X-Request-ID"));
    config.setExposedHeaders(List.of(
        "X-Request-ID", "X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
