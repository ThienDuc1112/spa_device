package com.company.device.factory;

import com.company.device.api.*;

public interface DeviceAlarmProvider {
  boolean supports(DeviceInfo info);

  DeviceAlarmAdapter create();

  DeviceInfoProvider deviceInfo();
}
