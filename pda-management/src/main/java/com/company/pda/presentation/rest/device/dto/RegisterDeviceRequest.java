package com.company.pda.presentation.rest.device.dto;

import com.company.pda.application.device.dto.RegisterDeviceCommand;
import java.util.*;
import javax.validation.constraints.*;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class RegisterDeviceRequest {
  private final @NotBlank @Size(max = 100) String deviceCode;
  private final @NotBlank @Size(max = 255) String deviceName;
  private final @Size(max = 4096) String fcmToken;

  @java.beans.ConstructorProperties({"deviceCode", "deviceName", "fcmToken"})
  public RegisterDeviceRequest(String deviceCode, String deviceName, String fcmToken) {
    this.deviceCode = deviceCode;
    this.deviceName = deviceName;
    this.fcmToken = fcmToken;
  }

  public String deviceCode() {
    return deviceCode;
  }

  public String getDeviceCode() {
    return deviceCode;
  }

  public String deviceName() {
    return deviceName;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public String fcmToken() {
    return fcmToken;
  }

  public String getFcmToken() {
    return fcmToken;
  }

  public RegisterDeviceCommand toCommand() {
    return new RegisterDeviceCommand(
        deviceCode,
        deviceName,
        fcmToken == null || fcmToken.codePoints().allMatch(Character::isWhitespace)
            ? null
            : fcmToken);
  }
}
