package com.company.device.android;

import static org.junit.Assert.*;

import android.content.Context;
import android.media.AudioManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class AndroidVolumeControllerTest {
  private Context context;
  private AudioManager audio;

  @Before
  public void setup() {
    context = RuntimeEnvironment.getApplication();
    audio = context.getSystemService(AudioManager.class);
  }

  @Test
  public void restoresMutedAlarmAndOriginalVolumeWithoutChangingSilentRinger() {
    audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    audio.setStreamVolume(AudioManager.STREAM_ALARM, 2, 0);
    audio.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
    var volume = new AndroidVolumeController(context);
    volume.maximize();
    assertFalse(audio.isStreamMute(AudioManager.STREAM_ALARM));
    assertEquals(
        audio.getStreamMaxVolume(AudioManager.STREAM_ALARM),
        audio.getStreamVolume(AudioManager.STREAM_ALARM));
    assertEquals(AudioManager.RINGER_MODE_SILENT, audio.getRingerMode());
    volume.restore();
    assertTrue(audio.isStreamMute(AudioManager.STREAM_ALARM));
    assertEquals(2, audio.getStreamVolume(AudioManager.STREAM_ALARM));
    assertEquals(AudioManager.RINGER_MODE_SILENT, audio.getRingerMode());
  }

  @Test
  public void recoversVolumeAfterControllerIsLost() {
    audio.setStreamVolume(AudioManager.STREAM_ALARM, 1, 0);
    new AndroidVolumeController(context).maximize();
    AndroidVolumeController.recover(context);
    assertEquals(1, audio.getStreamVolume(AudioManager.STREAM_ALARM));
    assertFalse(context.getSharedPreferences("finder_audio_recovery", 0).contains("volume"));
  }

  @Test
  public void detectsVolumeMutedDuringPlayback() {
    var volume = new AndroidVolumeController(context);
    volume.maximize();
    audio.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
    assertThrows(IllegalStateException.class, volume::verify);
    volume.restore();
  }

  @Test
  public void pendingRecoveryCannotBeOverwrittenByAnotherSession() {
    var recovery = context.getSharedPreferences("finder_audio_recovery", 0);
    recovery.edit().putInt("volume", 1).putBoolean("muted", false).commit();
    assertThrows(
        IllegalStateException.class, () -> new AndroidVolumeController(context).maximize());
    assertEquals(1, recovery.getInt("volume", -1));
    AndroidVolumeController.recover(context);
    assertEquals(1, audio.getStreamVolume(AudioManager.STREAM_ALARM));
  }
}
