package com.company.pda.application.device.dto;

import com.company.pda.domain.device.model.Device;
import com.company.pda.domain.device.model.DeviceStatus;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class DeviceResult {
  private final long id;
  private final String deviceCode;
  private final String deviceName;
  private final boolean reachable;
  private final String lastActiveAt;

  @java.beans.ConstructorProperties({"id", "deviceCode", "deviceName", "reachable", "lastActiveAt"})
  public DeviceResult(
      long id, String deviceCode, String deviceName, boolean reachable, String lastActiveAt) {
    this.id = id;
    this.deviceCode = deviceCode;
    this.deviceName = deviceName;
    this.reachable = reachable;
    this.lastActiveAt = lastActiveAt;
  }

  public long id() {
    return id;
  }

  public long getId() {
    return id;
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

  public boolean reachable() {
    return reachable;
  }

  public boolean getReachable() {
    return reachable;
  }

  public String lastActiveAt() {
    return lastActiveAt;
  }

  public String getLastActiveAt() {
    return lastActiveAt;
  }

  public static DeviceResult from(Device d) {
    return new DeviceResult(
        d.id(),
        d.deviceCode(),
        d.deviceName(),
        d.status() == DeviceStatus.REGISTERED,
        d.lastActiveAt() == null ? "" : d.lastActiveAt().toString());
  }
}
