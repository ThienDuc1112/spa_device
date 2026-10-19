package com.company.pda.application.port.out;

import com.company.pda.domain.auth.model.User;

public interface TokenProvider {
  String access(
      User user,
      java.util.List<String> roles,
      java.time.Instant now,
      java.time.Instant expiresAt,
      java.util.UUID family);

  String refresh(User user, java.util.UUID id, java.time.Instant now, java.time.Instant expiresAt);

  java.util.UUID verifyRefresh(String token);
}
