package com.company.pda.presentation.rest.pdafinder.dto;

import com.company.pda.application.pdafinder.dto.PdaAlertEventCommand;
import java.util.*;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class PdaAlertEventRequest {
  private final @NotNull UUID requestId;
  private final @Pattern(regexp = "RINGING|STOPPED|FAILED") @NotNull String status;

  @java.beans.ConstructorProperties({"requestId", "status"})
  public PdaAlertEventRequest(UUID requestId, String status) {
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

  public PdaAlertEventCommand toCommand() {
    return new PdaAlertEventCommand(requestId, status);
  }
}
