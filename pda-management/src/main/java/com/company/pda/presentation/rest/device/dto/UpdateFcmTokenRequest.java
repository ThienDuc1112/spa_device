package com.company.pda.presentation.rest.device.dto;

import com.company.pda.application.device.dto.UpdateFcmTokenCommand;
import java.util.*;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class UpdateFcmTokenRequest {
  private final @NotBlank @Size(max = 4096) String fcmToken;

  @java.beans.ConstructorProperties({"fcmToken"})
  public UpdateFcmTokenRequest(String fcmToken) {
    this.fcmToken = fcmToken;
  }

  public String fcmToken() {
    return fcmToken;
  }

  public String getFcmToken() {
    return fcmToken;
  }

  public UpdateFcmTokenCommand toCommand() {
    return new UpdateFcmTokenCommand(fcmToken);
  }
}
