package com.company.device.android;

import static org.junit.Assert.*;

import com.company.device.api.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class AndroidAlarmAdapterTest {
  @Test
  public void playbackFailureRestoresVolumeAndReleasesFocus() {
    var calls = new ArrayList<String>();
    var player =
        new AlarmPlayer() {
          public void play(String uri) throws Exception {
            calls.add("play");
            throw new Exception("Unavailable");
          }

          public void stop() {
            calls.add("stop");
          }
        };
    var volume =
        new VolumeController() {
          public void maximize() {
            calls.add("max");
          }

          public void restore() {
            calls.add("restore");
          }
        };
    var focus =
        new AudioModeController() {
          public boolean acquire(Runnable lost) {
            calls.add("focus");
            return true;
          }

          public void release() {
            calls.add("release");
          }
        };
    var adapter = new AndroidAlarmAdapter(player, volume, focus);
    assertEquals(
        AlarmStatus.FAILED,
        adapter
            .start(new AlarmPolicy("test", System.currentTimeMillis() + 10000, true), () -> {})
            .status());
    assertEquals(
        List.of("stop", "release", "restore", "focus", "max", "play", "stop", "release", "restore"),
        calls);
  }

  @Test
  public void expiredAlarmCannotAcquireFocusOrPlay() {
    var player =
        new AlarmPlayer() {
          public void play(String uri) {
            fail("Expired alarm played");
          }

          public void stop() {}
        };
    var volume =
        new VolumeController() {
          public void maximize() {
            fail("Volume changed");
          }

          public void restore() {}
        };
    var focus =
        new AudioModeController() {
          public boolean acquire(Runnable lost) {
            fail("Focus requested");
            return true;
          }

          public void release() {}
        };
    assertEquals(
        AlarmStatus.EXPIRED,
        new AndroidAlarmAdapter(player, volume, focus)
            .start(new AlarmPolicy(null, 0, true), () -> {})
            .status());
  }

  @Test
  public void focusDenialCannotMaximizeVolumeOrPlay() {
    var player =
        new AlarmPlayer() {
          public void play(String uri) {
            fail("Playback without focus");
          }

          public void stop() {}
        };
    var volume =
        new VolumeController() {
          public void maximize() {
            fail("Volume changed");
          }

          public void restore() {}
        };
    var focus =
        new AudioModeController() {
          public boolean acquire(Runnable lost) {
            return false;
          }

          public void release() {}
        };
    assertEquals(
        AlarmStatus.FAILED,
        new AndroidAlarmAdapter(player, volume, focus)
            .start(new AlarmPolicy(null, System.currentTimeMillis() + 10000, true), () -> {})
            .status());
  }
}
