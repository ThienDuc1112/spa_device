package com.company.device.factory;

import android.content.Context;
import com.company.device.android.AlarmAudioPolicy;
import com.company.device.android.AndroidVolumeController;

public final class DeviceAudioSetup {
  private DeviceAudioSetup() {}

  public static String blockingReason(Context context) {
    return AlarmAudioPolicy.blockingReason(context);
  }

  public static void recover(Context context) {
    AndroidVolumeController.recover(context);
  }

  public static boolean restorationPending(Context context) {
    return context
        .getSharedPreferences("finder_audio_recovery", Context.MODE_PRIVATE)
        .contains("volume");
  }
}
