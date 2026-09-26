package com.company.device.android;

import android.content.Context;
import com.company.device.api.*;

public class AndroidAlarmAdapter implements DeviceAlarmAdapter {
  private final AlarmPlayer player;
  private final VolumeController volume;
  private final AudioModeController mode;
  private boolean active;
  private static AndroidAlarmAdapter owner;

  public AndroidAlarmAdapter(Context context) {
    this(
        new AndroidAlarmPlayer(context),
        new AndroidVolumeController(context),
        new AndroidAudioModeController(context));
  }

  public AndroidAlarmAdapter(
      AlarmPlayer player, VolumeController volume, AudioModeController mode) {
    this.player = player;
    this.volume = volume;
    this.mode = mode;
  }

  public AlarmResult start(AlarmPolicy policy, Runnable onFocusLost) {
    stop();
    if (policy.remainingMillis(System.currentTimeMillis()) == 0)
      return new AlarmResult(AlarmStatus.EXPIRED, "Alarm deadline passed");
    synchronized (AndroidAlarmAdapter.class) {
      if (owner != null && owner != this)
        return new AlarmResult(AlarmStatus.FAILED, "Another alarm or sound test is active");
      owner = this;
    }
    try {
      if (!mode.acquire(onFocusLost)) throw new IllegalStateException("Audio focus denied");
      if (policy.maximizeVolume()) volume.maximize();
      player.play(policy.soundUri());
      active = true;
      return check();
    } catch (Exception e) {
      stop();
      return new AlarmResult(
          AlarmStatus.FAILED, e.getMessage() == null ? "Alarm audio unavailable" : e.getMessage());
    }
  }

  public void stop() {
    active = false;
    try {
      player.stop();
    } finally {
      try {
        mode.release();
      } finally {
        try {
          volume.restore();
        } catch (RuntimeException ignored) {
          // Persisted volume snapshot is retried at the next application start.
        } finally {
          synchronized (AndroidAlarmAdapter.class) {
            if (owner == this) owner = null;
          }
        }
      }
    }
  }

  @Override
  public AlarmResult check() {
    if (!active) return new AlarmResult(AlarmStatus.STOPPED, null);
    try {
      mode.verify();
      volume.verify();
      player.verify();
      return new AlarmResult(
          AlarmStatus.RINGING,
          "Playback active; physical speaker audibility requires a local sound test");
    } catch (RuntimeException e) {
      stop();
      return new AlarmResult(AlarmStatus.FAILED, e.getMessage());
    }
  }
}
