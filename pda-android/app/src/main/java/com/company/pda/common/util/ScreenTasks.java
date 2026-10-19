package com.company.pda.common.util;

import android.os.*;
import androidx.lifecycle.*;
import com.company.pda.PdaApplication;
import com.company.pda.common.logging.AppLogger;
import com.company.pda.di.AppModule;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

public final class ScreenTasks {
  public final MutableLiveData<String> status = new MutableLiveData<>("Ready");
  public final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
  public final MutableLiveData<String> error = new MutableLiveData<>();
  private final AppModule modules;
  private final BaseActivity activity;
  private Runnable retry;
  private Runnable pendingUi;
  private final Lifecycle lifecycle;
  private boolean cleared;

  public ScreenTasks(android.content.Context context, LifecycleOwner owner) {
    modules = ((PdaApplication) context.getApplicationContext()).modules();
    android.content.Context host = owner instanceof BaseActivity ? (BaseActivity) owner : context;
    while (!(host instanceof BaseActivity) && host instanceof android.content.ContextWrapper) {
      host = ((android.content.ContextWrapper) host).getBaseContext();
    }
    if (!(host instanceof BaseActivity))
      throw new IllegalArgumentException("ScreenTasks requires a BaseActivity host");
    activity = (BaseActivity) host;
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

  public interface Work<T> {
    T execute() throws Exception;
  }

  public <T> void request(
      String label, Supplier<CompletableFuture<T>> request, HttpReceiver<T> httpReceiver) {
    runAsync(label, request, httpReceiver);
  }

  public <T> void run(String label, Work<T> work, HttpReceiver<T> httpReceiver) {
    runAsync(
        label,
        () ->
            CompletableFuture.supplyAsync(
                () -> {
                  try {
                    return work.execute();
                  } catch (Exception error) {
                    throw new CompletionException(error);
                  }
                },
                modules.io),
        httpReceiver);
  }

  public <T> void runAsync(
      String label, Supplier<CompletableFuture<T>> work, HttpReceiver<T> httpReceiver) {
    if (cleared || Boolean.TRUE.equals(busy.getValue())) return;
    busy.setValue(true);
    status.setValue(label);
    retry = () -> runAsync(label, work, httpReceiver);
    CompletableFuture<T> future;
    try {
      future = work.get();
    } catch (Exception failure) {
      future = new CompletableFuture<>();
      future.completeExceptionally(failure);
    }
    future.whenComplete(
        (result, failure) -> {
          Throwable cause = failure;
          while (cause instanceof CompletionException && cause.getCause() != null)
            cause = cause.getCause();
          Exception exception =
              cause == null
                  ? null
                  : cause instanceof Exception ? (Exception) cause : new RuntimeException(cause);
          if (exception != null) AppLogger.failure(label, exception);
          new Handler(Looper.getMainLooper())
              .post(
                  () ->
                      deliver(
                          () -> {
                            busy.setValue(false);
                            if (activity.httpReceiver(httpReceiver, result, exception)) return;
                            status.setValue("Action did not complete");
                            error.setValue(
                                exception instanceof java.io.IOException
                                    ? exception.getMessage()
                                    : "Unable to complete action");
                          }));
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
