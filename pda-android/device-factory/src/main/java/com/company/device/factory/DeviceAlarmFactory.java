package com.company.device.factory;

import android.content.Context;
import com.company.device.android.*;
import com.company.device.api.*;
import com.company.device.urovo.*;
import com.company.device.zebra.*;
import java.util.*;
import java.util.function.Supplier;

public class DeviceAlarmFactory {
  private final List<DeviceAlarmProvider> providers;
  private final DeviceInfo info;

  public DeviceAlarmFactory(List<DeviceAlarmProvider> providers, DeviceInfo info) {
    this.providers = List.copyOf(providers);
    this.info = info;
  }

  public static DeviceAlarmFactory forAndroid(Context context) {
    return new DeviceAlarmFactory(
        List.of(
            provider("zebra", new ZebraDeviceAdapter(), () -> new ZebraAlarmAdapter(context)),
            provider("urovo", new UrovoDeviceAdapter(), () -> new UrovoAlarmAdapter(context)),
            provider("", new AndroidDeviceInfoProvider(), () -> new AndroidAlarmAdapter(context))),
        new AndroidDeviceInfoProvider().getDeviceInfo());
  }

  private static DeviceAlarmProvider provider(
      String manufacturer, DeviceInfoProvider info, Supplier<DeviceAlarmAdapter> factory) {
    return new DeviceAlarmProvider() {
      public boolean supports(DeviceInfo d) {
        return manufacturer.isEmpty()
            || d.manufacturer().toLowerCase(Locale.ROOT).contains(manufacturer);
      }

      public DeviceAlarmAdapter create() {
        return factory.get();
      }

      public DeviceInfoProvider deviceInfo() {
        return info;
      }
    };
  }

  private DeviceAlarmProvider selected() {
    return providers.stream()
        .filter(p -> p.supports(info))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No device adapter"));
  }

  public DeviceAlarmAdapter create() {
    return selected().create();
  }

  public DeviceInfoProvider deviceInfo() {
    return selected().deviceInfo();
  }
}
