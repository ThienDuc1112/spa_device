package com.company.pda.infrastructure.alarm;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.company.pda.PdaApplication;
import com.company.pda.infrastructure.firebase.SyncWorker;
import com.company.pda.presentation.home.HomeActivity;

public class PdaAlarmService extends Service {

  private static final String TAG = "PdaAlarm";
  private long lastAudioLogMs;
  public static volatile String activeId;

  public static final String SHOW = "com.company.pda.SHOW_ALARM";

  private static final String CHANNEL = "finder";

  private static final int NOTIFICATION = 41;

  private final AlarmTimeoutManager timeout = new AlarmTimeoutManager();
  private com.company.device.api.DeviceAlarmAdapter adapter;
  private String requestId;
  private boolean failed;
  private Vibrator vibrator;
  private final Handler audioMonitor = new Handler(Looper.getMainLooper());

  private static void channel(Context c) {

    var channel =
        new NotificationChannel(CHANNEL, "PDA finder", NotificationManager.IMPORTANCE_HIGH);

    channel.setDescription("Locate this store PDA");

    c.getSystemService(NotificationManager.class).createNotificationChannel(channel);
  }

  private static NotificationCompat.Builder notification(Context c, String message) {

    channel(c);

    var open =
        PendingIntent.getActivity(
            c,
            0,
            new Intent(c, HomeActivity.class),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    return new NotificationCompat.Builder(c, CHANNEL)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle("PDA finder")
        .setContentText(message)
        .setContentIntent(open)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_ALARM);
  }

  public static void notifyFallback(Context c, String id) {
    Log.w(TAG, "show fallback notification requestId=" + id);

    try {

      c.getSystemService(NotificationManager.class)
          .notify(
              NOTIFICATION,
              notification(c, "Finder request received. Open Store PDA.")
                  .setAutoCancel(true)
                  .build());

    } catch (SecurityException error) {
      Log.e(TAG, "fallback notification denied requestId=" + id, error);
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    Log.i(
        TAG,
        "onStartCommand startId="
            + startId
            + " action="
            + (intent == null ? null : intent.getAction())
            + " activeId="
            + activeId);
    if (intent == null) {
      Log.w(TAG, "stopping: null restart intent");

      stopSelf();

      return START_NOT_STICKY;
    }

    if ("STOP".equals(intent.getAction())) {
      Log.i(TAG, "stopping: notification STOP requestId=" + requestId);

      stopSelf();

      return START_NOT_STICKY;
    }

    String incoming = intent.getStringExtra("requestId");

    long deadline = intent.getLongExtra("expiresAt", 0);
    Log.i(
        TAG,
        "incoming requestId="
            + incoming
            + " deadlineMs="
            + deadline
            + " nowMs="
            + System.currentTimeMillis());

    var app = (PdaApplication) getApplication();

    var stop =
        PendingIntent.getService(
            this,
            0,
            new Intent(this, PdaAlarmService.class).setAction("STOP"),
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

    var alert =
        notification(this, "This PDA is being located")
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Stop alarm", stop)
            .build();

    if (Build.VERSION.SDK_INT >= 29)
      startForeground(
          NOTIFICATION,
          alert,
          android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
    else startForeground(NOTIFICATION, alert);

    Log.i(
        TAG,
        "foreground alarm started requestId="
            + incoming
            + " notificationsEnabled="
            + getSystemService(NotificationManager.class).areNotificationsEnabled());
    if (incoming == null
        || deadline <= System.currentTimeMillis()
        || app.modules().tokens.get("handled:" + incoming) != null) {

      Log.w(
          TAG,
          "rejected requestId="
              + incoming
              + " missingId="
              + (incoming == null)
              + " expired="
              + (deadline <= System.currentTimeMillis())
              + " handled="
              + (incoming != null && app.modules().tokens.get("handled:" + incoming) != null));
      if (requestId == null) stopSelf();

      return START_NOT_STICKY;
    }

    if (incoming.equals(requestId)) {
      Log.d(TAG, "ignored duplicate active requestId=" + incoming);
      return START_NOT_STICKY;
    }

    release();

    requestId = incoming;
    failed = false;

    activeId = incoming;

    adapter = com.company.pda.di.DeviceModule.alarm(this);
    var policy =
        new com.company.device.api.AlarmPolicy(
            "android.resource://" + getPackageName() + "/raw/pda_alarm", deadline, true);
    Log.i(
        TAG,
        "audio adapter="
            + adapter.getClass().getSimpleName()
            + " requestId="
            + requestId
            + " remainingMs="
            + (deadline - System.currentTimeMillis()));
    var result = adapter.start(policy, () -> failAlarm("Alarm interrupted: audio focus lost"));
    Log.i(
        TAG,
        "audio start result requestId="
            + requestId
            + " status="
            + result.status()
            + " detail="
            + result.message());
    if (result.ringing()) {
      startVibration();
      SyncWorker.event(app, requestId, "RINGING");
      sendBroadcast(new Intent(SHOW).setPackage(getPackageName()));
      Log.i(TAG, "schedule timeout requestId=" + requestId + " deadlineMs=" + deadline);
      timeout.schedule(
          policy,
          () -> {
            Log.i(TAG, "stopping: deadline reached requestId=" + requestId);
            stopSelf();
          });
      audioMonitor.postDelayed(this::checkAudio, 500);
    } else {
      failAlarm(result.message());
    }
    return START_NOT_STICKY;
  }

  private void checkAudio() {
    if (adapter == null || failed) return;
    var result = adapter.check();
    long now = SystemClock.elapsedRealtime();
    if (!result.ringing() || now - lastAudioLogMs >= 10000) {
      Log.d(
          TAG,
          "audio health requestId="
              + requestId
              + " status="
              + result.status()
              + " detail="
              + result.message());
      lastAudioLogMs = now;
    }
    if (!result.ringing()) failAlarm(result.message());
    else audioMonitor.postDelayed(this::checkAudio, 500);
  }

  private void startVibration() {
    try {
      if (Build.VERSION.SDK_INT >= 31) {
        var manager = getSystemService(VibratorManager.class);
        vibrator = manager == null ? null : manager.getDefaultVibrator();
      } else {
        vibrator = getSystemService(Vibrator.class);
      }
      if (vibrator == null || !vibrator.hasVibrator()) {
        Log.w(TAG, "vibration unavailable: no vibrator hardware requestId=" + requestId);
        vibrator = null;
        return;
      }
      // Repeat: vibrate 500 ms, pause 500 ms, until the alarm session is released.
      var effect = VibrationEffect.createWaveform(new long[] {0, 500, 500}, 0);
      if (Build.VERSION.SDK_INT >= 33) {
        vibrator.vibrate(
            effect,
            new VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_ALARM).build());
      } else {
        vibrator.vibrate(
            effect, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
      }
      Log.i(
          TAG,
          "vibration requested requestId="
              + requestId
              + " patternMs=500/500 sdk="
              + Build.VERSION.SDK_INT);
    } catch (RuntimeException e) {
      // Vibration is supplementary: unsupported hardware/policy must not stop the sound.
      android.util.Log.w("PdaAlarm", "Finder vibration unavailable requestId=" + requestId, e);
    }
  }

  private void stopVibration() {
    if (vibrator == null) return;
    try {
      vibrator.cancel();
      Log.i(TAG, "vibration cancelled requestId=" + requestId);
    } catch (RuntimeException e) {
      android.util.Log.w("PdaAlarm", "Cannot cancel finder vibration", e);
    } finally {
      vibrator = null;
    }
  }

  private void failAlarm(String reason) {
    if (requestId == null || failed) return;
    Log.e(TAG, "alarm failed requestId=" + requestId + " reason=" + reason);
    failed = true;
    SyncWorker.event((PdaApplication) getApplication(), requestId, "FAILED");
    getSharedPreferences("finder_sound", MODE_PRIVATE)
        .edit()
        .putString("last_failure", reason == null ? "Alarm audio unavailable" : reason)
        .apply();
    stopSelf();
  }

  private void release() {

    Log.i(TAG, "release requestId=" + requestId + " failed=" + failed);
    timeout.cancel();
    audioMonitor.removeCallbacksAndMessages(null);
    stopVibration();
    if (adapter != null) {
      Log.i(TAG, "audio stop requestId=" + requestId);
      adapter.stop();
      adapter = null;
    }
    if (requestId != null) {

      var app = (PdaApplication) getApplication();

      app.modules().tokens.put("handled:" + requestId, "true");

      if (!failed) SyncWorker.event(app, requestId, "STOPPED");

      requestId = null;
    }
  }

  @Override
  public void onDestroy() {
    Log.i(TAG, "onDestroy requestId=" + requestId);

    release();

    activeId = null;

    stopForeground(STOP_FOREGROUND_REMOVE);

    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {

    return null;
  }
}
