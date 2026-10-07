package com.company.pda.infrastructure.firebase;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.company.pda.BuildConfig;
import com.company.pda.PdaApplication;
import com.company.pda.common.exception.ApiException;
import com.company.pda.presentation.home.HomeActivity;
import java.util.concurrent.*;
import retrofit2.Call;

/** Persistent finder transport for managed PDAs; requires fleet battery-policy configuration. */
public final class FinderPollingService extends Service {
  private static final String TAG = "FinderPolling";
  private long pollNumber;
  private static final String CHANNEL = "finder-polling";
  private static final int NOTIFICATION = 42;
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final Handler main = new Handler(Looper.getMainLooper());
  private volatile boolean destroyed;
  private volatile Call<?> activeCall;
  private PowerManager.WakeLock wakeLock;
  private boolean started;
  private long delaySeconds = 5;
  private FinderPollingCycle cycle;
  private String cycleDeviceId, cycleSecret;

  /** Invoke from a visible activity or registration callback, never Application.onCreate. */
  public static void start(Context context) {
    Log.i(
        TAG,
        "start requested transport="
            + BuildConfig.FINDER_TRANSPORT
            + " sdk="
            + Build.VERSION.SDK_INT);
    if (!"polling".equals(BuildConfig.FINDER_TRANSPORT)) {
      Log.i(TAG, "polling disabled: build transport=" + BuildConfig.FINDER_TRANSPORT);
      context.stopService(new Intent(context, FinderPollingService.class));
      return;
    }
    var app = (PdaApplication) context.getApplicationContext();
    if (!app.modules().device.registered()) {
      Log.w(TAG, "start skipped: device not registered; register device then open Home");
      return;
    }
    try {
      ContextCompat.startForegroundService(
          context, new Intent(context, FinderPollingService.class));
    } catch (IllegalStateException | SecurityException e) {
      Log.e(TAG, "foreground service start rejected; open Home and check service permissions", e);
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(
        TAG,
        "onStartCommand startId="
            + startId
            + " stickyRestart="
            + (intent == null)
            + " alreadyStarted="
            + started);
    // Also guard direct starts and a sticky restart after installing an FCM build.
    if (!"polling".equals(BuildConfig.FINDER_TRANSPORT)) {
      stopSelf();
      return START_NOT_STICKY;
    }
    var manager = getSystemService(NotificationManager.class);
    Log.i(TAG, "foreground setup notificationsEnabled=" + manager.areNotificationsEnabled());
    manager.createNotificationChannel(
        new NotificationChannel(
            CHANNEL, "Finder availability", NotificationManager.IMPORTANCE_LOW));
    var open =
        PendingIntent.getActivity(
            this,
            0,
            new Intent(this, HomeActivity.class),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    var notification =
        new NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("PDA finder is available")
            .setContentText("Receiving finder commands via polling")
            .setContentIntent(open)
            .setOngoing(true)
            .setSilent(true)
            .build();
    if (Build.VERSION.SDK_INT >= 34)
      startForeground(NOTIFICATION, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
    else startForeground(NOTIFICATION, notification);
    Log.i(TAG, "foreground started");
    if (!((PdaApplication) getApplication()).modules().device.registered()) {
      Log.w(TAG, "stopping: device not registered");
      stopSelf();
      return START_NOT_STICKY;
    }
    if (!started) {
      started = true;
      wakeLock =
          getSystemService(PowerManager.class)
              .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StorePda:FinderPolling");
      wakeLock.setReferenceCounted(false);
      // Bounded lease, renewed by the polling loop and released on service destruction.
      wakeLock.acquire(TimeUnit.MINUTES.toMillis(10));
      Log.i(TAG, "poll loop starting wakeLockHeld=" + wakeLock.isHeld());
      executor.execute(this::poll);
    }
    return START_STICKY;
  }

  private void poll() {
    if (destroyed) return;
    final long number = ++pollNumber;
    final long startedAt = SystemClock.elapsedRealtime();
    var app = (PdaApplication) getApplication();
    var tokens = app.modules().tokens;
    String id = tokens.get("deviceId"), secret = tokens.get("deviceSecret");
    Log.d(TAG, "poll=" + number + " deviceId=" + id + " secretPresent=" + (secret != null));
    if (id == null || secret == null) {
      Log.w(TAG, "stopping: missing device credentials");
      main.post(this::stopSelf);
      return;
    }
    try {
      if (cycle == null || !id.equals(cycleDeviceId) || !secret.equals(cycleSecret)) {
        cycleDeviceId = id;
        cycleSecret = secret;
        cycle =
            new FinderPollingCycle(
                () -> execute(app.modules().finderApi.commands(Long.parseLong(id), secret)),
                "polling".equals(BuildConfig.FINDER_TRANSPORT));
      }
      var commands = cycle.run();
      if (commands == null) throw new java.io.IOException("Missing command response");
      Log.d(
          TAG,
          "poll="
              + number
              + " commands="
              + commands.size()
              + " elapsedMs="
              + (SystemClock.elapsedRealtime() - startedAt));
      main.post(
          () -> {
            if (destroyed
                || !id.equals(tokens.get("deviceId"))
                || !secret.equals(tokens.get("deviceSecret"))) {
              Log.w(
                  TAG,
                  "discard response poll=" + number + ": service destroyed or credentials changed");
              return;
            }
            for (var command : commands) {
              if (command != null)
                FinderCommandHandler.handle(
                    app, command.requestId, command.command, command.expiresAt, true);
              else Log.w(TAG, "poll=" + number + " ignored null command entry");
            }
          });
      delaySeconds = cycle.delaySeconds();
    } catch (ApiException e) {
      Log.w(
          TAG,
          "poll="
              + number
              + " HTTP failure status="
              + e.status
              + " elapsedMs="
              + (SystemClock.elapsedRealtime() - startedAt));
      if (e.status == 401 || e.status == 403) {
        Log.e(TAG, "stopping: authentication rejected; register device again then open Home");
        main.post(this::stopSelf);
        return;
      }
      delaySeconds = Math.min(60, delaySeconds * 2);
    } catch (Exception e) {
      Log.e(
          TAG,
          "poll=" + number + " failed elapsedMs=" + (SystemClock.elapsedRealtime() - startedAt),
          e);
      delaySeconds = Math.min(60, delaySeconds * 2);
    } finally {
      activeCall = null;
    }
    synchronized (this) {
      if (!destroyed) {
        wakeLock.acquire(TimeUnit.MINUTES.toMillis(10));
        Log.d(
            TAG,
            "poll="
                + number
                + " nextDelaySeconds="
                + delaySeconds
                + " wakeLockHeld="
                + wakeLock.isHeld());
        executor.schedule(this::poll, delaySeconds, TimeUnit.SECONDS);
      }
    }
  }

  private <T> T execute(Call<T> call) throws java.io.IOException {
    synchronized (this) {
      if (destroyed) throw new java.io.IOException("Finder monitor stopped");
      activeCall = call;
    }
    Log.d(
        TAG,
        "HTTP send GET /pda/commands host="
            + call.request().url().host()
            + " port="
            + call.request().url().port()
            + " scheme="
            + call.request().url().scheme());
    T body = com.company.pda.common.util.ApiCalls.execute(call);
    Log.d(TAG, "HTTP success GET /pda/commands bodyPresent=" + (body != null));
    return body;
  }

  @Override
  public synchronized void onDestroy() {
    Log.i(
        TAG,
        "onDestroy cancellingHttp="
            + (activeCall != null)
            + " wakeLockHeld="
            + (wakeLock != null && wakeLock.isHeld()));
    destroyed = true;
    var call = activeCall;
    if (call != null) call.cancel();
    executor.shutdownNow();
    main.removeCallbacksAndMessages(null);
    if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
    stopForeground(STOP_FOREGROUND_REMOVE);
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
