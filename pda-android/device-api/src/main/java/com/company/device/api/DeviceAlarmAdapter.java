package com.company.device.api;

public interface DeviceAlarmAdapter {
  AlarmResult start(AlarmPolicy policy, Runnable onFocusLost);

  void stop();

  AlarmResult check();
}
