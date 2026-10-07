package com.company.device.android;

import android.content.Context;
import android.media.AudioManager;
import com.company.device.api.VolumeController;

public class AndroidVolumeController implements VolumeController {
  private final AudioManager audio;
  private int previous = -1;
  private boolean wasMuted;
  private final android.content.SharedPreferences recovery;

  public AndroidVolumeController(Context context) {
    audio = context.getSystemService(AudioManager.class);
    recovery = context.getSharedPreferences("finder_audio_recovery", Context.MODE_PRIVATE);
  }

  public void maximize() {
    android.util.Log.i(
        "FinderAudio",
        "maximize alarm volume current="
            + audio.getStreamVolume(AudioManager.STREAM_ALARM)
            + " max="
            + audio.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            + " muted="
            + audio.isStreamMute(AudioManager.STREAM_ALARM));
    if (previous < 0) {
      if (recovery.contains("volume"))
        throw new IllegalStateException(
            "Previous alarm volume restoration is pending; reopen the app after allowing alarm"
                + " audio");
      previous = audio.getStreamVolume(AudioManager.STREAM_ALARM);
      wasMuted = audio.isStreamMute(AudioManager.STREAM_ALARM);
      if (!recovery.edit().putInt("volume", previous).putBoolean("muted", wasMuted).commit())
        throw new IllegalStateException("Cannot save alarm volume for restoration");
    }
    audio.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
    if (wasMuted) {
      // getStreamVolume can return zero while muted; capture the retained index after unmute.
      previous = audio.getStreamVolume(AudioManager.STREAM_ALARM);
      if (!recovery.edit().putInt("volume", previous).commit())
        throw new IllegalStateException("Cannot save unmuted alarm volume");
    }
    audio.setStreamVolume(
        AudioManager.STREAM_ALARM, audio.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
    verify();
    android.util.Log.i(
        "FinderAudio",
        "alarm volume maximized=" + audio.getStreamVolume(AudioManager.STREAM_ALARM));
  }

  @Override
  public void verify() {
    if (audio.isStreamMute(AudioManager.STREAM_ALARM)
        || audio.getStreamVolume(AudioManager.STREAM_ALARM) <= 0)
      throw new IllegalStateException("Device policy keeps alarm audio muted");
  }

  /** Recover after process death; a failed restoration is retried on the next app start. */
  public static void recover(Context context) {
    var controller = new AndroidVolumeController(context);
    controller.previous = controller.recovery.getInt("volume", -1);
    controller.wasMuted = controller.recovery.getBoolean("muted", false);
    try {
      controller.restore();
    } catch (RuntimeException error) {
      android.util.Log.e(
          "FinderAudio", "volume recovery failed; will retry at next app start", error);
    }
  }

  public void restore() {
    if (previous >= 0)
      try {
        android.util.Log.i(
            "FinderAudio", "restore alarm volume previous=" + previous + " muted=" + wasMuted);
        audio.setStreamVolume(AudioManager.STREAM_ALARM, previous, 0);
        // Check before reapplying mute, since muted streams may report zero volume.
        if (audio.getStreamVolume(AudioManager.STREAM_ALARM) != previous)
          throw new IllegalStateException("Device policy prevented alarm volume restoration");
        audio.adjustStreamVolume(
            AudioManager.STREAM_ALARM,
            wasMuted ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE,
            0);
        if (audio.isStreamMute(AudioManager.STREAM_ALARM) != wasMuted)
          throw new IllegalStateException("Device policy prevented alarm mute restoration");
        recovery.edit().clear().commit();
      } catch (RuntimeException error) {
        android.util.Log.e("FinderAudio", "volume restore failed; saved snapshot retained", error);
        throw error;
      } finally {
        previous = -1;
      }
  }
}
