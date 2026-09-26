package com.company.device.android;

import static org.junit.Assert.*;

import com.company.device.api.*;
import org.junit.Test;

public class AlarmHealthTest {
  private static class Audio implements AlarmPlayer, VolumeController, AudioModeController {
    boolean blocked, playing, maximized, focused;

    public void play(String uri) {
      playing = true;
    }

    public void stop() {
      playing = false;
    }

    public void maximize() {
      maximized = true;
    }

    public void restore() {
      maximized = false;
    }

    public boolean acquire(Runnable lost) {
      focused = true;
      return true;
    }

    public void release() {
      focused = false;
    }

    public void verify() {
      if (blocked) throw new IllegalStateException("DND blocked");
    }

    AndroidAlarmAdapter adapter() {
      return new AndroidAlarmAdapter(this, this, this);
    }
  }

  private AlarmPolicy policy() {
    return new AlarmPolicy("test", System.currentTimeMillis() + 10000, true);
  }

  @Test
  public void blockedPolicyCannotReturnRingingAndRestoresAudio() {
    var audio = new Audio();
    audio.blocked = true;
    var adapter = audio.adapter();
    try {
      assertEquals(AlarmStatus.FAILED, adapter.start(policy(), () -> {}).status());
      assertFalse(audio.playing);
      assertFalse(audio.maximized);
      assertFalse(audio.focused);
    } finally {
      adapter.stop();
    }
  }

  @Test
  public void policyChangeDuringPlaybackFailsAndCleansUp() {
    var audio = new Audio();
    var adapter = audio.adapter();
    try {
      assertTrue(adapter.start(policy(), () -> {}).ringing());
      audio.blocked = true;
      assertEquals(AlarmStatus.FAILED, adapter.check().status());
      assertFalse(audio.playing);
      assertFalse(audio.maximized);
      assertFalse(audio.focused);
    } finally {
      adapter.stop();
    }
  }

  @Test
  public void localTestCannotOverwriteActiveAlarmVolumeSnapshot() {
    var audio = new Audio();
    var first = audio.adapter();
    var second = new Audio().adapter();
    try {
      assertTrue(first.start(policy(), () -> {}).ringing());
      assertEquals(AlarmStatus.FAILED, second.start(policy(), () -> {}).status());
      assertTrue(first.check().ringing());
      first.stop();
      assertTrue(second.start(policy(), () -> {}).ringing());
    } finally {
      first.stop();
      second.stop();
    }
  }
}
