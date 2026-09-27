package com.company.pda.application.device.dto;

import com.company.pda.domain.device.model.Device;
import com.company.pda.domain.device.model.DeviceStatus;

public record DeviceResult(
    long id, String deviceCode, String deviceName, boolean reachable, String lastActiveAt) {
  public static DeviceResult from(Device d) {
    return new DeviceResult(
        d.id(),
        d.deviceCode(),
        d.deviceName(),
        d.status() == DeviceStatus.REGISTERED,
        d.lastActiveAt() == null ? "" : d.lastActiveAt().toString());
  }
}
