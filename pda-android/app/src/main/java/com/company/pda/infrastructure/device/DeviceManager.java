package com.company.pda.infrastructure.device;

import com.company.device.api.*;
import com.company.pda.data.local.preferences.TokenStorage;

public final class DeviceManager {
  private final DeviceInfoProvider info;
  private final TokenStorage storage;

  public DeviceManager(DeviceInfoProvider info, TokenStorage storage) {
    this.info = info;
    this.storage = storage;
  }

  public DeviceInfo info() {
    return info.getDeviceInfo();
  }

  public synchronized boolean registered() {
    return storage.get("deviceId") != null && storage.get("deviceSecret") != null;
  }

  public synchronized void registered(long id, String secret) {
    storage.put("deviceSecret", secret);
    storage.put("deviceId", Long.toString(id));
  }

  public synchronized boolean clearRegistration(String id, String secret) {
    if (!java.util.Objects.equals(id, storage.get("deviceId"))
        || !java.util.Objects.equals(secret, storage.get("deviceSecret"))) return false;
    storage.put("deviceId", null);
    storage.put("deviceSecret", null);
    return true;
  }
}
