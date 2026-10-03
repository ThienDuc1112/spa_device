package com.company.pda.application.pdafinder.dto;

import java.util.UUID;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class PdaAlertEventCommand {
  private final UUID requestId;
  private final String status;

  @java.beans.ConstructorProperties({"requestId", "status"})
  public PdaAlertEventCommand(UUID requestId, String status) {
    this.requestId = requestId;
    this.status = status;
  }

  public UUID requestId() {
    return requestId;
  }

  public UUID getRequestId() {
    return requestId;
  }

  public String status() {
    return status;
  }

  public String getStatus() {
    return status;
  }
}
