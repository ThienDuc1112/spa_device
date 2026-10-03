package com.company.pda.application.device.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class DeviceRegistrationResult {
  private final long deviceId;
  private final String deviceSecret;

  @java.beans.ConstructorProperties({"deviceId", "deviceSecret"})
  public DeviceRegistrationResult(long deviceId, String deviceSecret) {
    this.deviceId = deviceId;
    this.deviceSecret = deviceSecret;
  }

  public long deviceId() {
    return deviceId;
  }

  public long getDeviceId() {
    return deviceId;
  }

  public String deviceSecret() {
    return deviceSecret;
  }

  public String getDeviceSecret() {
    return deviceSecret;
  }
}
