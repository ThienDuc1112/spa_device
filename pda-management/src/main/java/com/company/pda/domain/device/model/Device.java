package com.company.pda.domain.device.model;

import java.time.Instant;

public record Device(
    long id,
    String deviceCode,
    String deviceName,
    long storeId,
    String credentialHash,
    String fcmToken,
    Instant lastActiveAt) {
  public DeviceStatus status() {
    return fcmToken == null ? DeviceStatus.PUSH_UNAVAILABLE : DeviceStatus.REGISTERED;
  }
}
