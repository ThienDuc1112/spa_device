package com.company.pda.di;

import android.content.Context;
import com.company.device.api.*;
import com.company.device.factory.DeviceAlarmFactory;

public final class DeviceModule {
  public static DeviceAlarmAdapter alarm(Context context) {
    return DeviceAlarmFactory.forAndroid(context).create();
  }

  public static DeviceInfoProvider info(Context context) {
    return DeviceAlarmFactory.forAndroid(context).deviceInfo();
  }
}
