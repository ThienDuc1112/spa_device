package com.company.pda.presentation.rest.pdafinder.dto;

import com.company.pda.application.pdafinder.dto.PdaAlertEventCommand;
import jakarta.validation.constraints.*;
import java.util.*;

public record PdaAlertEventRequest(
    @NotNull UUID requestId, @Pattern(regexp = "RINGING|STOPPED|FAILED") @NotNull String status) {
  public PdaAlertEventCommand toCommand() {
    return new PdaAlertEventCommand(requestId, status);
  }
}
