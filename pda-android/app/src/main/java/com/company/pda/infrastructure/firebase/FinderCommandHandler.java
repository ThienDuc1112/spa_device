package com.company.pda.infrastructure.firebase;

import android.util.Log;
import com.company.pda.PdaApplication;
import com.company.pda.infrastructure.alarm.PdaAlarmService;

/** Called on the main thread by both transports, including STOP tombstones. */
public final class FinderCommandHandler {
  private static final String TAG = "FinderCommand";

  public static void handle(
      PdaApplication app, String id, String command, String expiresAt, boolean mayStart) {
    Log.i(
        TAG,
        "received requestId="
            + id
            + " command="
            + command
            + " expiresAt="
            + expiresAt
            + " mayStart="
            + mayStart
            + " activeId="
            + PdaAlarmService.activeId);
    try {
      java.util.UUID.fromString(id);
    } catch (Exception e) {
      Log.w(TAG, "rejected: invalid requestId=" + id);
      return;
    }
    if ("STOP".equals(command)) {
      Log.i(TAG, "dispatch STOP requestId=" + id);
      app.modules().alarm.stop(id);
      return;
    }
    if (!"FIND".equals(command)) {
      Log.w(TAG, "ignored: unsupported command=" + command + " requestId=" + id);
      return;
    }
    if (app.modules().tokens.get("handled:" + id) != null || id.equals(PdaAlarmService.activeId)) {
      Log.d(TAG, "ignored: already handled or active requestId=" + id);
      return;
    }
    long expiry;
    try {
      expiry = java.time.Instant.parse(expiresAt).toEpochMilli();
    } catch (Exception e) {
      Log.w(TAG, "rejected: invalid expiresAt=" + expiresAt + " requestId=" + id);
      return;
    }
    long now = System.currentTimeMillis();
    if (expiry <= now) {
      Log.w(TAG, "ignored: expired requestId=" + id + " deadlineMs=" + expiry + " nowMs=" + now);
      return;
    }
    if (!mayStart) {
      Log.w(TAG, "fallback: transport cannot start alarm requestId=" + id);
      PdaAlarmService.notifyFallback(app, id);
      return;
    }
    try {
      Log.i(TAG, "dispatch FIND requestId=" + id + " remainingMs=" + (expiry - now));
      app.modules().alarm.start(id, expiry);
    } catch (RuntimeException e) {
      Log.e(TAG, "foreground alarm start failed requestId=" + id, e);
      PdaAlarmService.notifyFallback(app, id);
      // Do not mark handled: a later delivery in the configured transport may retry before expiry.
    }
  }
}
