package com.company.pda;

import static org.junit.Assert.*;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.company.pda.infrastructure.alarm.PdaAlarmService;
import com.company.pda.infrastructure.firebase.FinderCommandHandler;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FinderCommandTest {
  @Test
  public void tokenRecoveryDoesNotHideAnUnrecoveredDeliveryFailure() {
    PdaApplication app = ApplicationProvider.getApplicationContext();
    var tokens = app.modules().tokens;
    String originalToken = tokens.get("fcmToken"), originalFailure = tokens.get("fcmFailure");
    try {
      tokens.put("fcmToken", null);
      assertTrue(app.modules().fcm.unavailable());
      app.modules().fcm.failed("TOKEN_ERROR");
      app.modules().fcm.refreshed("local-test-token");
      assertFalse(app.modules().fcm.unavailable());
      app.modules().fcm.failed("DELIVERY_ERROR");
      app.modules().fcm.refreshed("local-test-token");
      assertTrue(app.modules().fcm.unavailable());
      app.modules().fcm.received();
      assertFalse(app.modules().fcm.unavailable());
    } finally {
      tokens.put("fcmToken", originalToken);
      tokens.put("fcmFailure", originalFailure);
    }
  }

  @Test
  public void pollingRemainsForegroundAfterActivityCloses() {
    PdaApplication app = ApplicationProvider.getApplicationContext();
    var tokens = app.modules().tokens;
    String originalId = tokens.get("deviceId"), originalSecret = tokens.get("deviceSecret");
    try {
      tokens.put("deviceId", "9223372036854775806");
      tokens.put("deviceSecret", "polling-instrumentation-fixture");
      if (android.os.Build.VERSION.SDK_INT >= 33)
        InstrumentationRegistry.getInstrumentation()
            .getUiAutomation()
            .grantRuntimePermission(
                app.getPackageName(), android.Manifest.permission.POST_NOTIFICATIONS);
      try (var scenario =
          androidx.test.core.app.ActivityScenario.launch(
              com.company.pda.presentation.pdafinder.FinderSoundActivity.class)) {
        scenario.onActivity(com.company.pda.infrastructure.firebase.FinderPollingService::start);
        long deadline = android.os.SystemClock.uptimeMillis() + 5000;
        while (!pollingForeground(app) && android.os.SystemClock.uptimeMillis() < deadline)
          android.os.SystemClock.sleep(50);
        assertTrue(pollingForeground(app));
      }
      InstrumentationRegistry.getInstrumentation().waitForIdleSync();
      assertTrue(pollingForeground(app));
    } finally {
      app.stopService(
          new android.content.Intent(
              app, com.company.pda.infrastructure.firebase.FinderPollingService.class));
      tokens.put("deviceId", originalId);
      tokens.put("deviceSecret", originalSecret);
    }
  }

  @SuppressWarnings("deprecation")
  private boolean pollingForeground(PdaApplication app) {
    // Android may defer rendering an FGS notification; inspect our own service state instead.
    return app.getSystemService(android.app.ActivityManager.class).getRunningServices(100).stream()
        .anyMatch(
            s ->
                s.foreground
                    && s.service
                        .getClassName()
                        .equals(
                            com.company.pda.infrastructure.firebase.FinderPollingService.class
                                .getName()));
  }

  @Test
  public void stopBeforeFindAndDuplicateStopCannotRestartAlarm() {
    PdaApplication app = ApplicationProvider.getApplicationContext();
    String id = UUID.randomUUID().toString();
    try {
      InstrumentationRegistry.getInstrumentation()
          .runOnMainSync(
              () -> {
                FinderCommandHandler.handle(app, id, "STOP", null, true);
                FinderCommandHandler.handle(app, id, "STOP", null, true);
                FinderCommandHandler.handle(
                    app, id, "FIND", Instant.now().plusSeconds(60).toString(), true);
              });
      InstrumentationRegistry.getInstrumentation().waitForIdleSync();
      assertEquals("true", app.modules().tokens.get("handled:" + id));
      assertNotEquals(id, PdaAlarmService.activeId);
    } finally {
      app.modules().tokens.put("handled:" + id, null);
    }
  }

  @Test
  public void expiredOrMalformedFindDoesNotStartAlarm() {
    PdaApplication app = ApplicationProvider.getApplicationContext();
    String id = UUID.randomUUID().toString();
    InstrumentationRegistry.getInstrumentation()
        .runOnMainSync(
            () -> {
              FinderCommandHandler.handle(
                  app, id, "FIND", Instant.now().minusSeconds(1).toString(), true);
              FinderCommandHandler.handle(app, id, "FIND", "invalid", true);
              FinderCommandHandler.handle(
                  app, id, "UNKNOWN", Instant.now().plusSeconds(60).toString(), true);
              FinderCommandHandler.handle(app, null, "STOP", null, true);
            });
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    assertNull(app.modules().tokens.get("handled:" + id));
    assertNotEquals(id, PdaAlarmService.activeId);
  }
}
