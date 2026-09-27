package com.company.pda.presentation.rest.disposal.dto;

import com.company.pda.application.disposal.dto.DisposalTransitionCommand;
import jakarta.validation.constraints.*;
import java.util.*;

public record DisposalTransitionRequest(@NotNull @PositiveOrZero Long version) {
  public DisposalTransitionCommand toCommand() {
    return new DisposalTransitionCommand(version);
  }
}
