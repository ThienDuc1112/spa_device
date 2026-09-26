package com.company.pda.presentation.pdafinder;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.company.device.api.AlarmPolicy;
import com.company.device.api.DeviceAlarmAdapter;
import com.company.device.factory.DeviceAudioSetup;
import com.company.pda.common.util.ScreenViews;
import com.company.pda.di.DeviceModule;

/** Setup is performed locally on each PDA that must be findable. */
public class FinderSoundActivity extends AppCompatActivity {
  private final Handler handler = new Handler(Looper.getMainLooper());
  private DeviceAlarmAdapter adapter;
  private TextView status;

  @Override
  public void onCreate(Bundle saved) {
    super.onCreate(saved);
  }

  @Override
  public void onResume() {
    super.onResume();
    var box = ScreenViews.column(this);
    var scroll = new android.widget.ScrollView(this);
    scroll.addView(box);
    setContentView(scroll);
    com.company.pda.common.util.ScreenSupport.insets(scroll);
    var ui = new ScreenViews(box);
    ui.text("Finder sound on this PDA", 22);
    ui.text(
        "Finder uses alarm audio in silent and vibrate modes. In Do Not Disturb, allow alarms in"
            + " every active mode. Sound tests play at maximum alarm volume for five seconds.",
        16);
    status = new TextView(this);
    box.addView(status);
    String reason = DeviceAudioSetup.blockingReason(this);
    status.setText(
        reason == null ? "Alarm policy allows playback. Test the physical speaker below." : reason);
    var manager = getSystemService(NotificationManager.class);
    ui.text(
        "Do Not Disturb access: "
            + (manager.isNotificationPolicyAccessGranted() ? "granted" : "not granted"),
        14);
    String lastFailure =
        getSharedPreferences("finder_sound", MODE_PRIVATE).getString("last_failure", null);
    if (lastFailure != null) ui.text("Last finder audio failure: " + lastFailure, 14);
    ui.button(
        "Grant Do Not Disturb access",
        () -> open(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
    ui.button(
        "Configure Do Not Disturb / allow alarms",
        () -> open("android.settings.ZEN_MODE_SETTINGS"));
    ui.button("Test this PDA speaker (5 seconds)", this::testSound);
    ui.button(
        "Stop sound test",
        () -> {
          stopTest();
          status.setText(restorationMessage());
        });
    ui.text(
        "Repeat this test in silent, vibrate and your usual Do Not Disturb modes. If no sound is"
            + " heard, check device management restrictions and the speaker. RINGING means playback"
            + " passed software checks, not that someone heard it.",
        14);
    ui.button("Back", this::finish);
  }

  private void open(String action) {
    try {
      startActivity(new Intent(action));
    } catch (android.content.ActivityNotFoundException e) {
      try {
        startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
      } catch (android.content.ActivityNotFoundException missing) {
        status.setText(
            "This device does not expose that settings screen. Ask the device administrator to"
                + " allow alarm audio.");
      }
    }
  }

  private void testSound() {
    stopTest();
    adapter = DeviceModule.alarm(this);
    var result =
        adapter.start(
            new AlarmPolicy(
                "android.resource://" + getPackageName() + "/raw/pda_alarm",
                System.currentTimeMillis() + 5000,
                true),
            () -> {
              stopTest();
              status.setText("Sound test interrupted: audio focus lost.");
            });
    status.setText(
        result.ringing() ? "Playing through the PDA speaker. Can you hear it?" : result.message());
    if (result.ringing()) {
      handler.postDelayed(this::checkTest, 500);
      handler.postDelayed(
          () -> {
            stopTest();
            status.setText(restorationMessage() + " Confirm that you heard the speaker.");
          },
          5000);
    } else stopTest();
  }

  private void checkTest() {
    if (adapter == null) return;
    var result = adapter.check();
    if (!result.ringing()) {
      stopTest();
      status.setText(result.message());
    } else handler.postDelayed(this::checkTest, 500);
  }

  private void stopTest() {
    handler.removeCallbacksAndMessages(null);
    if (adapter != null) {
      adapter.stop();
      adapter = null;
    }
  }

  private String restorationMessage() {
    return DeviceAudioSetup.restorationPending(this)
        ? "Sound stopped. Volume restoration was blocked; allow alarm audio and reopen the app."
        : "Sound stopped; alarm volume restored.";
  }

  @Override
  public void onPause() {
    stopTest();
    super.onPause();
  }
}
