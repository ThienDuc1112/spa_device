package com.company.pda.application.pdafinder.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class FindPdaCommand {
  private final long deviceId;

  @java.beans.ConstructorProperties({"deviceId"})
  public FindPdaCommand(long deviceId) {
    this.deviceId = deviceId;
  }

  public long deviceId() {
    return deviceId;
  }

  public long getDeviceId() {
    return deviceId;
  }
}
