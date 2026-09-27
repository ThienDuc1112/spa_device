package com.company.pda.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface NotificationPort {
  void send(String token, String command, UUID requestId, Instant expiresAt);

  class InvalidToken extends RuntimeException {}
}
