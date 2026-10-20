package com.company.pda.infrastructure.security;

import com.company.pda.domain.auth.repository.AuthRepository;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  SecretKeySpec jwtKey(@Value("${app.jwt-secret}") String secret) {
    byte[] key = java.util.Base64.getDecoder().decode(secret);
    if (key.length < 32)
      throw new IllegalArgumentException("JWT_SECRET must be base64 of at least 32 random bytes");
    return new SecretKeySpec(key, "HmacSHA256");
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http, JwtTokenProvider tokens, AuthRepository sessions)
      throws Exception {
    return http.csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.antMatchers(
                        "/auth/login",
                        "/web/finder/**",
                        "/auth/refresh",
                        "/actuator/health",
                        "/devices/token",
                        "/pda/events",
                        "/pda/commands",
                        "/pda/fcm-health")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(new JsonAuthenticationEntryPoint())
                    .accessDeniedHandler(new AccessDeniedHandlerImpl()))
        .addFilterBefore(
            new JwtAuthenticationFilter(tokens, new SessionValidator(sessions)),
            UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
