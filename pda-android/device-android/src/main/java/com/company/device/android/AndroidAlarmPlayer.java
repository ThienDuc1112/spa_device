package com.company.device.android;

import android.content.Context;
import android.media.*;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;
import com.company.device.api.AlarmPlayer;

public class AndroidAlarmPlayer implements AlarmPlayer {
  private final Context context;
  private MediaPlayer player;

  public AndroidAlarmPlayer(Context context) {
    this.context = context.getApplicationContext();
  }

  public void play(String soundUri) throws Exception {
    stop();
    Log.i("FinderAudio", "MediaPlayer setup soundUri=" + soundUri);
    try {
      player = new MediaPlayer();
      var audio = context.getSystemService(AudioManager.class);
      AudioDeviceInfo speaker = null;
      for (var device : audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
        if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
          speaker = device;
          break;
        }
      }
      player.setAudioAttributes(
          new AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_ALARM)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build());
      Uri uri =
          soundUri == null
              ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
              : Uri.parse(soundUri);
      if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      Log.i(
          "FinderAudio",
          "MediaPlayer setDataSource uri=" + uri + " speakerFound=" + (speaker != null));
      player.setDataSource(context, uri);
      player.setLooping(true);
      player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
      Log.i("FinderAudio", "MediaPlayer prepare begin");
      player.prepare();
      Log.i("FinderAudio", "MediaPlayer prepare succeeded");
      // The native playback instance must exist before selecting its output device.
      if (speaker == null || !player.setPreferredDevice(speaker))
        throw new IllegalStateException("Cannot select the PDA speaker");
      Log.i("FinderAudio", "speaker selected; MediaPlayer start begin");
      player.start();
      Log.i("FinderAudio", "MediaPlayer start succeeded isPlaying=" + player.isPlaying());
    } catch (Exception error) {
      Log.e("FinderAudio", "MediaPlayer setup/start failed", error);
      throw error;
    }
  }

  @Override
  public void verify() {
    if (player == null || !player.isPlaying())
      throw new IllegalStateException("Alarm playback stopped");
    var output = player.getRoutedDevice();
    if (output != null && output.getType() != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
      throw new IllegalStateException("Alarm was routed away from the PDA speaker");
  }

  public void stop() {
    if (player != null) {
      Log.i("FinderAudio", "MediaPlayer release");
      player.release();
      player = null;
    }
  }
}
