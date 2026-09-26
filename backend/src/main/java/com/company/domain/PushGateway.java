package com.company.domain;

import java.time.Instant;
import java.util.UUID;

public interface PushGateway {
  void send(String token, String command, UUID requestId, Instant expiresAt);

  class InvalidToken extends RuntimeException {}
}
