package com.company.pda;

import static org.junit.Assert.*;

import android.os.Looper;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import com.company.pda.common.util.ScreenTasks;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

public class ScreenTasksTest {
  private static class Owner implements LifecycleOwner {
    final LifecycleRegistry lifecycle = new LifecycleRegistry(this);

    public Lifecycle getLifecycle() {
      return lifecycle;
    }
  }

  @Test
  public void workRunsOffMainAndResultRunsOnMain() throws Exception {
    var instrumentation = InstrumentationRegistry.getInstrumentation();
    var complete = new CountDownLatch(1);
    var workerOffMain = new AtomicBoolean();
    var resultOnMain = new AtomicBoolean();
    instrumentation.runOnMainSync(
        () -> {
          var owner = new Owner();
          owner.lifecycle.setCurrentState(Lifecycle.State.STARTED);
          var tasks = new ScreenTasks(ApplicationProvider.getApplicationContext(), owner);
          tasks.run(
              "Test",
              () -> {
                workerOffMain.set(Looper.myLooper() != Looper.getMainLooper());
                return () -> {
                  resultOnMain.set(Looper.myLooper() == Looper.getMainLooper());
                  owner.lifecycle.setCurrentState(Lifecycle.State.DESTROYED);
                  complete.countDown();
                };
              });
        });
    assertTrue(complete.await(5, TimeUnit.SECONDS));
    assertTrue(workerOffMain.get());
    assertTrue(resultOnMain.get());
  }

  @Test
  public void destroyedScreenDoesNotReceiveCompletedWork() throws Exception {
    var instrumentation = InstrumentationRegistry.getInstrumentation();
    var started = new CountDownLatch(1);
    var release = new CountDownLatch(1);
    var callback = new AtomicBoolean();
    Owner[] owner = new Owner[1];
    instrumentation.runOnMainSync(
        () -> {
          owner[0] = new Owner();
          owner[0].lifecycle.setCurrentState(Lifecycle.State.STARTED);
          var tasks = new ScreenTasks(ApplicationProvider.getApplicationContext(), owner[0]);
          tasks.run(
              "Test",
              () -> {
                started.countDown();
                if (!release.await(5, TimeUnit.SECONDS))
                  throw new IllegalStateException("Test timed out");
                return () -> callback.set(true);
              });
        });
    assertTrue(started.await(5, TimeUnit.SECONDS));
    instrumentation.runOnMainSync(
        () -> owner[0].lifecycle.setCurrentState(Lifecycle.State.DESTROYED));
    release.countDown();
    // Wait for the worker to post its result, then drain main-thread callbacks.
    PdaApplication app = ApplicationProvider.getApplicationContext();
    var executor = (java.util.concurrent.ThreadPoolExecutor) app.modules().io;
    long deadline = android.os.SystemClock.elapsedRealtime() + 5000;
    while (executor.getActiveCount() > 0 && android.os.SystemClock.elapsedRealtime() < deadline)
      Thread.sleep(10);
    assertEquals(0, executor.getActiveCount());
    instrumentation.waitForIdleSync();
    assertFalse(callback.get());
  }
}
