package com.company.pda.common.util;

import android.os.*;
import androidx.lifecycle.*;
import com.company.pda.PdaApplication;
import com.company.pda.common.logging.AppLogger;
import com.company.pda.di.AppModule;

public final class ScreenTasks {
  public final MutableLiveData<String> status = new MutableLiveData<>("Ready");
  public final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
  public final MutableLiveData<String> error = new MutableLiveData<>();
  private final AppModule modules;
  private Runnable retry;
  private Runnable pendingUi;
  private final Lifecycle lifecycle;
  private boolean cleared;

  public ScreenTasks(android.content.Context context, LifecycleOwner owner) {
    modules = ((PdaApplication) context.getApplicationContext()).modules();
    lifecycle = owner.getLifecycle();
    owner
        .getLifecycle()
        .addObserver(
            new DefaultLifecycleObserver() {
              public void onStart(LifecycleOwner owner) {
                if (pendingUi != null) {
                  Runnable action = pendingUi;
                  pendingUi = null;
                  action.run();
                }
              }

              public void onDestroy(LifecycleOwner owner) {
                cleared = true;
                retry = null;
                pendingUi = null;
              }
            });
  }

  public interface Work {
    Runnable execute() throws Exception;
  }

  public void run(String label, Work work) {
    if (cleared || Boolean.TRUE.equals(busy.getValue())) return;
    busy.setValue(true);
    status.setValue(label);
    retry = () -> run(label, work);
    modules.io.execute(
        () -> {
          try {
            var result = work.execute();
            new Handler(Looper.getMainLooper())
                .post(
                    () -> {
                      deliver(
                          () -> {
                            busy.setValue(false);
                            result.run();
                          });
                    });
          } catch (Exception e) {
            AppLogger.failure(label, e);
            new Handler(Looper.getMainLooper())
                .post(
                    () -> {
                      deliver(
                          () -> {
                            busy.setValue(false);
                            status.setValue("Action did not complete");
                            error.setValue(
                                e instanceof java.io.IOException
                                    ? e.getMessage()
                                    : "Unable to complete action");
                          });
                    });
          }
        });
  }

  public void retry() {
    if (retry != null) retry.run();
  }

  private void deliver(Runnable action) {
    if (cleared) return;
    if (lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) action.run();
    else pendingUi = action;
  }

  public void clearRetry() {
    retry = null;
  }
}
