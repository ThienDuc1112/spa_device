package com.company.pda.application.device.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class RegisterDeviceCommand {
  private final String deviceCode;
  private final String deviceName;
  private final String fcmToken;

  @java.beans.ConstructorProperties({"deviceCode", "deviceName", "fcmToken"})
  public RegisterDeviceCommand(String deviceCode, String deviceName, String fcmToken) {
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
}
