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

  public boolean registered() {
    return storage.get("deviceId") != null && storage.get("deviceSecret") != null;
  }

  public void registered(long id, String secret) {
    storage.put("deviceSecret", secret);
    storage.put("deviceId", Long.toString(id));
  }
}
