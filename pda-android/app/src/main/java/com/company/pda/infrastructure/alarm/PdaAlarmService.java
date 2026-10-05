package com.company.pda.infrastructure.alarm;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import androidx.core.app.NotificationCompat;
import com.company.pda.PdaApplication;
import com.company.pda.infrastructure.firebase.SyncWorker;
import com.company.pda.presentation.home.HomeActivity;

public class PdaAlarmService extends Service {

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

    try {

      c.getSystemService(NotificationManager.class)
          .notify(
              NOTIFICATION,
              notification(c, "Finder request received. Open Store PDA.")
                  .setAutoCancel(true)
                  .build());

    } catch (SecurityException ignored) {

    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    if (intent == null) {

      stopSelf();

      return START_NOT_STICKY;
    }

    if ("STOP".equals(intent.getAction())) {

      stopSelf();

      return START_NOT_STICKY;
    }

    String incoming = intent.getStringExtra("requestId");

    long deadline = intent.getLongExtra("expiresAt", 0);

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

    if (incoming == null
        || deadline <= System.currentTimeMillis()
        || app.modules().tokens.get("handled:" + incoming) != null) {

      if (requestId == null) stopSelf();

      return START_NOT_STICKY;
    }

    if (incoming.equals(requestId)) return START_NOT_STICKY;

    release();

    requestId = incoming;
    failed = false;

    activeId = incoming;

    adapter = com.company.pda.di.DeviceModule.alarm(this);
    var policy =
        new com.company.device.api.AlarmPolicy(
            "android.resource://" + getPackageName() + "/raw/pda_alarm", deadline, true);
    var result = adapter.start(policy, () -> failAlarm("Alarm interrupted: audio focus lost"));
    if (result.ringing()) {
      startVibration();
      SyncWorker.event(app, requestId, "RINGING");
      sendBroadcast(new Intent(SHOW).setPackage(getPackageName()));
      timeout.schedule(policy, this::stopSelf);
      audioMonitor.postDelayed(this::checkAudio, 500);
    } else {
      failAlarm(result.message());
    }
    return START_NOT_STICKY;
  }

  private void checkAudio() {
    if (adapter == null || failed) return;
    var result = adapter.check();
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
            effect,
            new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
      }
    } catch (RuntimeException e) {
      // Vibration is supplementary: unsupported hardware/policy must not stop the sound.
      android.util.Log.w("PdaAlarm", "Finder vibration unavailable", e);
    }
  }

  private void stopVibration() {
    if (vibrator == null) return;
    try {
      vibrator.cancel();
    } catch (RuntimeException e) {
      android.util.Log.w("PdaAlarm", "Cannot cancel finder vibration", e);
    } finally {
      vibrator = null;
    }
  }

  private void failAlarm(String reason) {
    if (requestId == null || failed) return;
    failed = true;
    SyncWorker.event((PdaApplication) getApplication(), requestId, "FAILED");
    getSharedPreferences("finder_sound", MODE_PRIVATE)
        .edit()
        .putString("last_failure", reason == null ? "Alarm audio unavailable" : reason)
        .apply();
    stopSelf();
  }

  private void release() {

    timeout.cancel();
    audioMonitor.removeCallbacksAndMessages(null);
    stopVibration();
    if (adapter != null) {
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
