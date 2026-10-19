package com.company.pda.infrastructure.security;

import com.company.pda.domain.auth.repository.AuthRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;

final class SessionValidator implements OAuth2TokenValidator<Jwt> {
  private final AuthRepository sessions;

  SessionValidator(AuthRepository sessions) {
    this.sessions = sessions;
  }

  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    if (jwt.getExpiresAt() == null || !jwt.getExpiresAt().isAfter(Instant.now()))
      return OAuth2TokenValidatorResult.failure(new OAuth2Error("ACCESS_TOKEN_EXPIRED"));
    String family = jwt.getClaimAsString("sessionId");
    // Tokens issued before this rollout remain usable until their signed expiry.
    if (family == null) return OAuth2TokenValidatorResult.success();
    try {
      if (sessions.activeSession(UUID.fromString(family), Long.parseLong(jwt.getSubject())))
        return OAuth2TokenValidatorResult.success();
    } catch (IllegalArgumentException ignored) {
    }
    return OAuth2TokenValidatorResult.failure(new OAuth2Error("SESSION_REVOKED"));
  }
}
