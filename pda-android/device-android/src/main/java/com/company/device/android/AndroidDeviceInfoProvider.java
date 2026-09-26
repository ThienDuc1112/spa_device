package com.company.device.android;

import android.os.Build;
import com.company.device.api.*;
import java.util.Set;

public class AndroidDeviceInfoProvider implements DeviceInfoProvider {
  public DeviceInfo getDeviceInfo() {
    return new DeviceInfo(
        Build.MANUFACTURER,
        Build.MODEL,
        Build.VERSION.SDK_INT,
        Set.of(
            DeviceCapability.ALARM_PLAYBACK,
            DeviceCapability.ALARM_VOLUME,
            DeviceCapability.AUDIO_FOCUS));
  }
}
