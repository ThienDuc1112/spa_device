package com.company.pda.presentation.rest.pdafinder.dto;

import com.company.pda.application.pdafinder.dto.StopPdaCommand;
import java.util.*;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class StopPdaRequest {
  private final @NotNull UUID requestId;

  @java.beans.ConstructorProperties({"requestId"})
  public StopPdaRequest(UUID requestId) {
    this.requestId = requestId;
  }

  public UUID requestId() {
    return requestId;
  }

  public UUID getRequestId() {
    return requestId;
  }

  public StopPdaCommand toCommand() {
    return new StopPdaCommand(requestId);
  }
}
