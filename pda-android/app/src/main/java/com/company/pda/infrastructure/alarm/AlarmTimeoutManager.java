package com.company.pda.infrastructure.alarm;

import android.os.*;
import com.company.device.api.AlarmPolicy;

public class AlarmTimeoutManager {
  private final Handler handler = new Handler(Looper.getMainLooper());

  public void schedule(AlarmPolicy policy, Runnable stop) {
    cancel();
    handler.postDelayed(stop, Math.max(1, policy.remainingMillis(System.currentTimeMillis())));
  }

  public void cancel() {
    handler.removeCallbacksAndMessages(null);
  }
}
