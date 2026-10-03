package com.company.pda.application.pdafinder.dto;

import java.time.Instant;
import java.util.UUID;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class DeviceFinderCommand {
  private final UUID requestId;
  private final String command;
  private final Instant expiresAt;

  @java.beans.ConstructorProperties({"requestId", "command", "expiresAt"})
  public DeviceFinderCommand(UUID requestId, String command, Instant expiresAt) {
    this.requestId = requestId;
    this.command = command;
    this.expiresAt = expiresAt;
  }

  public UUID requestId() {
    return requestId;
  }

  public UUID getRequestId() {
    return requestId;
  }

  public String command() {
    return command;
  }

  public String getCommand() {
    return command;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }
}
