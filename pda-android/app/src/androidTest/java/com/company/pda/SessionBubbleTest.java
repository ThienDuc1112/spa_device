package com.company.pda;

import static org.junit.Assert.*;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.company.pda.data.remote.dto.auth.AuthDto;
import com.company.pda.presentation.auth.LoginActivity;
import com.company.pda.presentation.home.HomeActivity;
import com.company.pda.presentation.pdafinder.FinderSoundActivity;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SessionBubbleTest {
  private PdaApplication app;

  @Before
  public void session() {
    app = ApplicationProvider.getApplicationContext();
    app.getSharedPreferences("session_bubble", 0).edit().clear().commit();
    var tokens = new AuthDto.Tokens();
    tokens.accessToken = "local-ui-test";
    tokens.refreshToken = "local-ui-test";
    tokens.sessionExpiresAt = System.currentTimeMillis() + 7200000;
    app.modules().tokens.tokens(tokens);
    if (android.os.Build.VERSION.SDK_INT >= 33) {
      InstrumentationRegistry.getInstrumentation()
          .getUiAutomation()
          .grantRuntimePermission(app.getPackageName(), "android.permission.POST_NOTIFICATIONS");
    }
  }

  @After
  public void cleanup() {
    app.modules().tokens.logout();
    app.getSharedPreferences("session_bubble", 0).edit().clear().commit();
  }

  @Test
  public void bubbleFollowsFeatureNavigationRotationAndSoundScreen() {
    try (var scenario = ActivityScenario.launch(HomeActivity.class)) {
      scenario.onActivity(
          activity -> {
            assertBubble(activity.findViewById(android.R.id.content));
            activity.navigate("disposal");
          });
      scenario.onActivity(activity -> assertBubble(activity.findViewById(android.R.id.content)));
      scenario.recreate();
      scenario.onActivity(
          activity -> {
            assertEquals("disposal", activity.screen);
            assertBubble(activity.findViewById(android.R.id.content));
          });
    }
    try (var scenario = ActivityScenario.launch(FinderSoundActivity.class)) {
      scenario.onActivity(activity -> assertBubble(activity.findViewById(android.R.id.content)));
    }
    app.modules().tokens.logout();
    try (var scenario = ActivityScenario.launch(LoginActivity.class)) {
      scenario.onActivity(activity -> assertNull(activity.findViewById(R.id.session_bubble)));
    }
  }

  @Test
  public void dragSnapsToEdgeAndSurvivesRecreationAndTapOpensDetails() {
    try (var scenario = ActivityScenario.launch(FinderSoundActivity.class)) {
      InstrumentationRegistry.getInstrumentation().waitForIdleSync();
      scenario.onActivity(
          activity -> {
            TextView bubble = activity.findViewById(R.id.session_bubble);
            ViewGroup root = (ViewGroup) bubble.getParent();
            long now = android.os.SystemClock.uptimeMillis();
            touch(bubble, now, MotionEvent.ACTION_DOWN, 0, 0);
            touch(
                bubble, now + 20, MotionEvent.ACTION_MOVE, -root.getWidth(), root.getHeight() / 3f);
            touch(bubble, now + 40, MotionEvent.ACTION_UP, -root.getWidth(), root.getHeight() / 3f);
            assertFalse(app.getSharedPreferences("session_bubble", 0).getBoolean("right", true));
          });
      scenario.recreate();
      InstrumentationRegistry.getInstrumentation().waitForIdleSync();
      scenario.onActivity(
          activity -> {
            TextView bubble = activity.findViewById(R.id.session_bubble);
            ViewGroup root = (ViewGroup) bubble.getParent();
            assertTrue(bubble.getX() < root.getWidth() / 2f);
            assertTrue(bubble.getY() + bubble.getHeight() <= root.getHeight());
            bubble.performClick();
          });
      InstrumentationRegistry.getInstrumentation().waitForIdleSync();
      var window =
          InstrumentationRegistry.getInstrumentation().getUiAutomation().getRootInActiveWindow();
      assertNotNull(window);
      assertFalse(window.findAccessibilityNodeInfosByText("Your session").isEmpty());
      assertFalse(window.findAccessibilityNodeInfosByText("Extend session").isEmpty());
      window.recycle();
    }
  }

  @Test
  public void expiringSessionShowsWarningAndExtensionAction() {
    var tokens = app.modules().tokens.tokens();
    tokens.sessionExpiresAt = System.currentTimeMillis() + 60000;
    app.modules().tokens.tokens(tokens);
    try (var scenario = ActivityScenario.launch(FinderSoundActivity.class)) {
      scenario.onActivity(
          activity -> {
            TextView bubble = activity.findViewById(R.id.session_bubble);
            assertTrue(bubble.getText().toString().startsWith("Expiring\n"));
          });
      InstrumentationRegistry.getInstrumentation().waitForIdleSync();
      var window =
          InstrumentationRegistry.getInstrumentation().getUiAutomation().getRootInActiveWindow();
      assertNotNull(window);
      assertFalse(window.findAccessibilityNodeInfosByText("Session expiring").isEmpty());
      assertFalse(window.findAccessibilityNodeInfosByText("Extend session").isEmpty());
      window.recycle();
    }
  }

  private void touch(View view, long time, int action, float x, float y) {
    MotionEvent event = MotionEvent.obtain(time, time, action, x, y, 0);
    view.dispatchTouchEvent(event);
    event.recycle();
  }

  private void assertBubble(View root) {
    TextView bubble = root.findViewById(R.id.session_bubble);
    assertNotNull(bubble);
    assertTrue(bubble.isShown());
    assertTrue(bubble.getText().toString().matches("Session\\n[0-9]{2}:[0-9]{2}:[0-9]{2}"));
    assertEquals(1, count(root));
  }

  private int count(View view) {
    int count = view.getId() == R.id.session_bubble ? 1 : 0;
    if (view instanceof ViewGroup group) {
      for (int i = 0; i < group.getChildCount(); i++) count += count(group.getChildAt(i));
    }
    return count;
  }
}
