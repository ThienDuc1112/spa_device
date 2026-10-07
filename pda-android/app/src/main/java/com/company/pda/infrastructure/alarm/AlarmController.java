package com.company.pda.infrastructure.alarm;

import android.content.*;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.company.pda.data.local.preferences.TokenStorage;

public class AlarmController {
  private final Context context;
  private final TokenStorage tokens;

  public AlarmController(Context context, TokenStorage tokens) {
    this.context = context.getApplicationContext();
    this.tokens = tokens;
  }

  public void start(String id, long expiry) {
    if (expiry <= System.currentTimeMillis() || tokens.get("handled:" + id) != null) {
      Log.w("PdaAlarm", "controller skipped expired/handled requestId=" + id);
      return;
    }
    Log.i("PdaAlarm", "controller startForegroundService requestId=" + id);
    ContextCompat.startForegroundService(
        context,
        new Intent(context, PdaAlarmService.class)
            .putExtra("requestId", id)
            .putExtra("expiresAt", expiry));
  }

  public void stop(String id) {
    Log.i(
        "PdaAlarm",
        "controller STOP requestId="
            + id
            + " activeId="
            + PdaAlarmService.activeId
            + " matchesActive="
            + id.equals(PdaAlarmService.activeId));
    tokens.put("handled:" + id, "true");
    if (id.equals(PdaAlarmService.activeId))
      context.stopService(new Intent(context, PdaAlarmService.class));
  }
}
