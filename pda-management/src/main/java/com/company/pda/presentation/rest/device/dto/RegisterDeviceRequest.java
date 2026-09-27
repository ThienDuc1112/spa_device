package com.company.pda.presentation.rest.device.dto;

import com.company.pda.application.device.dto.RegisterDeviceCommand;
import jakarta.validation.constraints.*;
import java.util.*;

public record RegisterDeviceRequest(
    @NotBlank @Size(max = 100) String deviceCode,
    @NotBlank @Size(max = 255) String deviceName,
    @NotBlank @Size(max = 4096) String fcmToken) {
  public RegisterDeviceCommand toCommand() {
    return new RegisterDeviceCommand(deviceCode, deviceName, fcmToken);
  }
}
