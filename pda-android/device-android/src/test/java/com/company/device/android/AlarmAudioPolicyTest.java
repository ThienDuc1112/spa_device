package com.company.device.android;

import static org.junit.Assert.*;

import android.app.NotificationManager;
import org.junit.Test;

public class AlarmAudioPolicyTest {
  @Test
  public void allowsUnrestrictedAndAlarmOnlyModes() {
    assertTrue(AlarmAudioPolicy.allowsAlarm(NotificationManager.INTERRUPTION_FILTER_ALL, false));
    assertTrue(AlarmAudioPolicy.allowsAlarm(NotificationManager.INTERRUPTION_FILTER_ALARMS, false));
  }

  @Test
  public void priorityModeRequiresAlarmException() {
    assertFalse(
        AlarmAudioPolicy.allowsAlarm(NotificationManager.INTERRUPTION_FILTER_PRIORITY, false));
    assertTrue(
        AlarmAudioPolicy.allowsAlarm(NotificationManager.INTERRUPTION_FILTER_PRIORITY, true));
  }

  @Test
  public void totalSilenceAndUnknownPolicyFailClosed() {
    assertFalse(AlarmAudioPolicy.allowsAlarm(NotificationManager.INTERRUPTION_FILTER_NONE, true));
    assertFalse(
        AlarmAudioPolicy.allowsAlarm(NotificationManager.INTERRUPTION_FILTER_UNKNOWN, true));
  }
}
