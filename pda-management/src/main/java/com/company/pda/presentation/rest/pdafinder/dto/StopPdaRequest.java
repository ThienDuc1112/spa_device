package com.company.pda.presentation.rest.pdafinder.dto;

import com.company.pda.application.pdafinder.dto.StopPdaCommand;
import jakarta.validation.constraints.*;
import java.util.*;

public record StopPdaRequest(@NotNull UUID requestId) {
  public StopPdaCommand toCommand() {
    return new StopPdaCommand(requestId);
  }
}
