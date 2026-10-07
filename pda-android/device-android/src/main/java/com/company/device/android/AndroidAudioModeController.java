package com.company.device.android;

import android.content.Context;
import android.media.*;
import com.company.device.api.AudioModeController;

/** Requests alarm focus without overriding DND or ringer mode. */
public class AndroidAudioModeController implements AudioModeController {
  private final AudioManager audio;
  private AudioFocusRequest focus;
  private final Context context;

  public AndroidAudioModeController(Context context) {
    this.context = context.getApplicationContext();
    audio = context.getSystemService(AudioManager.class);
  }

  public boolean acquire(Runnable onFocusLost) {
    release();
    android.util.Log.i(
        "FinderAudio",
        "audio focus acquire; checking DND policy ringerMode=" + audio.getRingerMode());
    verify();
    focus =
        new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
            .setOnAudioFocusChangeListener(
                change -> {
                  android.util.Log.i("FinderAudio", "audio focus change=" + change);
                  if (change < 0) onFocusLost.run();
                })
            .build();
    int result = audio.requestAudioFocus(focus);
    android.util.Log.i(
        "FinderAudio",
        "audio focus result="
            + result
            + " granted="
            + (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED));
    return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  }

  @Override
  public void verify() {
    String reason = AlarmAudioPolicy.blockingReason(context);
    if (reason != null) {
      android.util.Log.e("FinderAudio", "audio policy blocked reason=" + reason);
      throw new IllegalStateException(reason);
    }
  }

  public void release() {
    if (focus != null) {
      android.util.Log.i("FinderAudio", "audio focus release");
      audio.abandonAudioFocusRequest(focus);
      focus = null;
    }
  }
}
