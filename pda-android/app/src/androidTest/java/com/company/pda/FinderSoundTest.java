package com.company.pda;

import static org.junit.Assert.*;

import android.media.AudioManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.company.pda.presentation.pdafinder.FinderSoundActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FinderSoundTest {
  private String texts(View view) {
    StringBuilder text = new StringBuilder();
    if (view instanceof TextView label) text.append(label.getText()).append('\n');
    if (view instanceof ViewGroup group) {
      for (int i = 0; i < group.getChildCount(); i++) text.append(texts(group.getChildAt(i)));
    }
    return text.toString();
  }

  private void dnd(String mode) throws Exception {
    var automation =
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getUiAutomation();
    try (var output =
        new android.os.ParcelFileDescriptor.AutoCloseInputStream(
            automation.executeShellCommand("cmd notification set_dnd " + mode))) {
      byte[] buffer = new byte[1024];
      while (output.read(buffer) != -1) {}
    }
  }

  @Test
  public void totalSilenceShowsFailureAndDoesNotRaiseVolume() throws Exception {
    android.content.Context context =
        androidx.test.core.app.ApplicationProvider.getApplicationContext();
    var manager = context.getSystemService(android.app.NotificationManager.class);
    int original = manager.getCurrentInterruptionFilter();
    try {
      dnd("none");
      long deadline = android.os.SystemClock.uptimeMillis() + 5000;
      while (manager.getCurrentInterruptionFilter()
              != android.app.NotificationManager.INTERRUPTION_FILTER_NONE
          && android.os.SystemClock.uptimeMillis() < deadline) android.os.SystemClock.sleep(50);
      assertEquals(
          android.app.NotificationManager.INTERRUPTION_FILTER_NONE,
          manager.getCurrentInterruptionFilter());
      try (var scenario = ActivityScenario.launch(FinderSoundActivity.class)) {
        scenario.onActivity(
            activity -> {
              var audio = activity.getSystemService(AudioManager.class);
              int previous = audio.getStreamVolume(AudioManager.STREAM_ALARM);
              var root = activity.getWindow().getDecorView();
              button(root, "Test this PDA speaker (5 seconds)").performClick();
              assertTrue(contains(root, "Do Not Disturb blocks alarms"));
              assertFalse(contains(root, "Playing through the PDA speaker"));
              assertEquals(previous, audio.getStreamVolume(AudioManager.STREAM_ALARM));
            });
      }
    } finally {
      dnd(
          switch (original) {
            case 2 -> "priority";
            case 3 -> "none";
            case 4 -> "alarms";
            default -> "all";
          });
    }
  }

  private Button button(View view, String label) {
    if (view instanceof Button && ((Button) view).getText().toString().equals(label))
      return (Button) view;
    if (view instanceof ViewGroup group) {
      for (int i = 0; i < group.getChildCount(); i++) {
        Button found = button(group.getChildAt(i), label);
        if (found != null) return found;
      }
    }
    return null;
  }

  private boolean contains(View view, String text) {
    if (view instanceof TextView && ((TextView) view).getText().toString().contains(text))
      return true;
    if (view instanceof ViewGroup group) {
      for (int i = 0; i < group.getChildCount(); i++)
        if (contains(group.getChildAt(i), text)) return true;
    }
    return false;
  }

  @Test
  public void soundTestStartsAndStopRestoresVolume() {
    try (var scenario = ActivityScenario.launch(FinderSoundActivity.class)) {
      scenario.onActivity(
          activity -> {
            var audio = activity.getSystemService(AudioManager.class);
            int previous = audio.getStreamVolume(AudioManager.STREAM_ALARM);
            boolean muted = audio.isStreamMute(AudioManager.STREAM_ALARM);
            var root = activity.getWindow().getDecorView();
            try {
              audio.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
              audio.setStreamVolume(AudioManager.STREAM_ALARM, 2, 0);
              button(root, "Test this PDA speaker (5 seconds)").performClick();
              assertTrue(
                  "Expected playback, screen: " + texts(root),
                  contains(root, "Playing through the PDA speaker"));
              assertEquals(
                  audio.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                  audio.getStreamVolume(AudioManager.STREAM_ALARM));
              button(root, "Stop sound test").performClick();
              assertEquals(2, audio.getStreamVolume(AudioManager.STREAM_ALARM));
            } finally {
              button(root, "Stop sound test").performClick();
              audio.setStreamVolume(AudioManager.STREAM_ALARM, previous, 0);
              audio.adjustStreamVolume(
                  AudioManager.STREAM_ALARM,
                  muted ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE,
                  0);
            }
          });
    }
  }

  @Test
  public void leavingSoundScreenRestoresVolume() {
    final int[] previous = new int[1];
    try (var scenario = ActivityScenario.launch(FinderSoundActivity.class)) {
      scenario.onActivity(
          activity -> {
            var audio = activity.getSystemService(AudioManager.class);
            previous[0] = audio.getStreamVolume(AudioManager.STREAM_ALARM);
            button(activity.getWindow().getDecorView(), "Test this PDA speaker (5 seconds)")
                .performClick();
            assertTrue(
                texts(activity.getWindow().getDecorView()),
                contains(activity.getWindow().getDecorView(), "Playing through the PDA speaker"));
          });
      scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED);
      var context = androidx.test.core.app.ApplicationProvider.getApplicationContext();
      var audio = context.getSystemService(AudioManager.class);
      assertEquals(previous[0], audio.getStreamVolume(AudioManager.STREAM_ALARM));
    }
  }
}
