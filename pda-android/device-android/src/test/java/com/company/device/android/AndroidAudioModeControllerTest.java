package com.company.device.android;

import static org.junit.Assert.*;

import android.app.NotificationManager;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class AndroidAudioModeControllerTest {
  private Context context;
  private NotificationManager manager;

  @Before
  public void setup() {
    context = RuntimeEnvironment.getApplication();
    manager = context.getSystemService(NotificationManager.class);
    Shadows.shadowOf(manager).setNotificationPolicyAccessGranted(true);
  }

  @Test
  public void totalSilenceBlocksWithoutChangingDnd() {
    manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
    var mode = new AndroidAudioModeController(context);
    assertThrows(IllegalStateException.class, () -> mode.acquire(() -> {}));
    assertEquals(
        NotificationManager.INTERRUPTION_FILTER_NONE, manager.getCurrentInterruptionFilter());
  }

  @Test
  public void priorityModeWithoutAlarmExceptionBlocks() {
    manager.setNotificationPolicy(new NotificationManager.Policy(0, 0, 0));
    manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
    assertNotNull(AlarmAudioPolicy.blockingReason(context));
  }

  @Test
  public void priorityModeWithAlarmExceptionAllows() {
    manager.setNotificationPolicy(
        new NotificationManager.Policy(NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS, 0, 0));
    manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
    assertNull(AlarmAudioPolicy.blockingReason(context));
  }

  @Test
  public void revokedPolicyAccessCannotReportReady() {
    manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
    Shadows.shadowOf(manager).setNotificationPolicyAccessGranted(false);
    assertNotNull(AlarmAudioPolicy.blockingReason(context));
  }
}
