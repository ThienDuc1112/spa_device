package com.company.pda.presentation.rest.pdafinder.dto;

import com.company.pda.application.pdafinder.dto.FindPdaCommand;
import jakarta.validation.constraints.*;
import java.util.*;

public record FindPdaRequest(@Positive long deviceId) {
  public FindPdaCommand toCommand() {
    return new FindPdaCommand(deviceId);
  }
}
