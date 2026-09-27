package com.company.pda.presentation.rest.device.dto;

import com.company.pda.application.device.dto.UpdateFcmTokenCommand;
import jakarta.validation.constraints.*;
import java.util.*;

public record UpdateFcmTokenRequest(@NotBlank @Size(max = 4096) String fcmToken) {
  public UpdateFcmTokenCommand toCommand() {
    return new UpdateFcmTokenCommand(fcmToken);
  }
}
