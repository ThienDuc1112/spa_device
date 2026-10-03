package com.company.pda.domain.device.model;

import java.time.Instant;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class Device {
  private final long id;
  private final String deviceCode;
  private final String deviceName;
  private final long storeId;
  private final String credentialHash;
  private final String fcmToken;
  private final Instant lastActiveAt;

  @java.beans.ConstructorProperties({
    "id",
    "deviceCode",
    "deviceName",
    "storeId",
    "credentialHash",
    "fcmToken",
    "lastActiveAt"
  })
  public Device(
      long id,
      String deviceCode,
      String deviceName,
      long storeId,
      String credentialHash,
      String fcmToken,
      Instant lastActiveAt) {
    this.id = id;
    this.deviceCode = deviceCode;
    this.deviceName = deviceName;
    this.storeId = storeId;
    this.credentialHash = credentialHash;
    this.fcmToken = fcmToken;
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

  public long storeId() {
    return storeId;
  }

  public long getStoreId() {
    return storeId;
  }

  public String credentialHash() {
    return credentialHash;
  }

  public String getCredentialHash() {
    return credentialHash;
  }

  public String fcmToken() {
    return fcmToken;
  }

  public String getFcmToken() {
    return fcmToken;
  }

  public Instant lastActiveAt() {
    return lastActiveAt;
  }

  public Instant getLastActiveAt() {
    return lastActiveAt;
  }

  public DeviceStatus status() {
    return fcmToken == null ? DeviceStatus.PUSH_UNAVAILABLE : DeviceStatus.REGISTERED;
  }
}
