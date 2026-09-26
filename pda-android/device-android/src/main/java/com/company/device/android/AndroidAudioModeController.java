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
    verify();
    focus =
        new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
            .setOnAudioFocusChangeListener(
                change -> {
                  if (change < 0) onFocusLost.run();
                })
            .build();
    return audio.requestAudioFocus(focus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  }

  @Override
  public void verify() {
    String reason = AlarmAudioPolicy.blockingReason(context);
    if (reason != null) throw new IllegalStateException(reason);
  }

  public void release() {
    if (focus != null) {
      audio.abandonAudioFocusRequest(focus);
      focus = null;
    }
  }
}
