package com.company.pda.application.pdafinder.dto;

import java.util.UUID;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class StopPdaCommand {
  private final UUID requestId;

  @java.beans.ConstructorProperties({"requestId"})
  public StopPdaCommand(UUID requestId) {
    this.requestId = requestId;
  }

  public UUID requestId() {
    return requestId;
  }

  public UUID getRequestId() {
    return requestId;
  }
}
