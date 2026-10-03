package com.company.pda.infrastructure.firebase;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.company.pda.PdaApplication;
import com.company.pda.common.exception.ApiException;
import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto;
import com.company.pda.presentation.home.HomeActivity;
import java.util.List;
import java.util.concurrent.*;
import retrofit2.Call;

/** Persistent finder transport for managed PDAs; requires fleet battery-policy configuration. */
public final class FinderPollingService extends Service {
  private static final String CHANNEL = "finder-polling";
  private static final int NOTIFICATION = 42;
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final Handler main = new Handler(Looper.getMainLooper());
  private volatile boolean destroyed;
  private volatile Call<?> activeCall;
  private PowerManager.WakeLock wakeLock;
  private boolean started;
  private long delaySeconds = 15;
  private FinderPollingCycle cycle;
  private String cycleDeviceId, cycleSecret;
  private long nextTokenRetry;

  /** Invoke from a visible activity or registration callback, never Application.onCreate. */
  public static void start(Context context) {
    var app = (PdaApplication) context.getApplicationContext();
    if (!app.modules().device.registered()) return;
    try {
      ContextCompat.startForegroundService(
          context, new Intent(context, FinderPollingService.class));
    } catch (IllegalStateException | SecurityException e) {
      android.util.Log.w("FinderPolling", "Open Store PDA to start finder polling");
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    var manager = getSystemService(NotificationManager.class);
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
            .setContentText("Monitoring push availability; finder fallback is ready")
            .setContentIntent(open)
            .setOngoing(true)
            .setSilent(true)
            .build();
    if (Build.VERSION.SDK_INT >= 34)
      startForeground(NOTIFICATION, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
    else startForeground(NOTIFICATION, notification);
    if (!((PdaApplication) getApplication()).modules().device.registered()) {
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
      executor.execute(this::poll);
    }
    return START_STICKY;
  }

  private void poll() {
    if (destroyed) return;
    var app = (PdaApplication) getApplication();
    var tokens = app.modules().tokens;
    String id = tokens.get("deviceId"), secret = tokens.get("deviceSecret");
    if (id == null || secret == null) {
      main.post(this::stopSelf);
      return;
    }
    try {
      if (cycle == null || !id.equals(cycleDeviceId) || !secret.equals(cycleSecret)) {
        cycleDeviceId = id;
        cycleSecret = secret;
        cycle =
            new FinderPollingCycle(
                new FinderPollingCycle.Transport() {
                  public boolean backendFcmFailed() throws java.io.IOException {
                    var health =
                        execute(app.modules().finderApi.fcmHealth(Long.parseLong(id), secret));
                    if (health == null)
                      throw new java.io.IOException("Missing FCM health response");
                    return health.fallbackRequired;
                  }

                  public List<PdaFinderDto.Command> commands() throws java.io.IOException {
                    return execute(app.modules().finderApi.commands(Long.parseLong(id), secret));
                  }
                });
      }
      long now = SystemClock.elapsedRealtime();
      if (now >= nextTokenRetry) {
        nextTokenRetry = now + 60_000;
        app.modules().fcm.retryIfNeeded();
      }
      var commands = cycle.run(app.modules().fcm.unavailable(), now);
      if (commands == null) throw new java.io.IOException("Missing command response");
      main.post(
          () -> {
            if (destroyed
                || !id.equals(tokens.get("deviceId"))
                || !secret.equals(tokens.get("deviceSecret"))) return;
            for (var command : commands) {
              if (command != null)
                FinderCommandHandler.handle(
                    app, command.requestId, command.command, command.expiresAt, true);
            }
          });
      delaySeconds = cycle.delaySeconds();
    } catch (ApiException e) {
      if (e.status == 401 || e.status == 403) {
        main.post(this::stopSelf);
        return;
      }
      delaySeconds = Math.min(60, delaySeconds * 2);
    } catch (Exception e) {
      delaySeconds = Math.min(60, delaySeconds * 2);
    } finally {
      activeCall = null;
    }
    synchronized (this) {
      if (!destroyed) {
        wakeLock.acquire(TimeUnit.MINUTES.toMillis(10));
        executor.schedule(this::poll, delaySeconds, TimeUnit.SECONDS);
      }
    }
  }

  private <T> T execute(Call<T> call) throws java.io.IOException {
    synchronized (this) {
      if (destroyed) throw new java.io.IOException("Finder monitor stopped");
      activeCall = call;
    }
    return com.company.pda.common.util.ApiCalls.execute(call);
  }

  @Override
  public synchronized void onDestroy() {
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
