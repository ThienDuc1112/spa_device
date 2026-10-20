package com.company.pda.infrastructure.security;

import com.company.pda.domain.auth.repository.AuthRepository;
import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.util.UUID;

final class SessionValidator {
  private final AuthRepository sessions;

  SessionValidator(AuthRepository sessions) {
    this.sessions = sessions;
  }

  public void validate(Claims claims) {
    if (claims.getExpiration() == null
        || !claims.getExpiration().toInstant().isAfter(Instant.now()))
      throw new JwtAuthenticationException("ACCESS_TOKEN_EXPIRED");
    Object family = claims.get("sessionId");
    // Tokens issued before this rollout remain usable until their signed expiry.
    if (family == null) return;
    try {
      if (sessions.activeSession(
          UUID.fromString(family.toString()), Long.parseLong(claims.getSubject()))) return;
    } catch (IllegalArgumentException ignored) {
    }
    throw new JwtAuthenticationException("SESSION_REVOKED");
  }
}
