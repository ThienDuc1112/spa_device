package com.company.pda.common.util;

import android.app.Application;
import android.os.*;
import androidx.lifecycle.*;
import com.company.pda.PdaApplication;
import com.company.pda.common.logging.AppLogger;
import com.company.pda.di.AppModule;

public abstract class AsyncViewModel extends AndroidViewModel {
  public final MutableLiveData<String> status = new MutableLiveData<>("Ready");
  public final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
  public final MutableLiveData<String> error = new MutableLiveData<>();
  protected final AppModule modules;
  private Runnable retry;
  private boolean cleared;

  protected AsyncViewModel(Application app) {
    super(app);
    modules = ((PdaApplication) app).modules();
  }

  protected interface Work {
    Runnable execute() throws Exception;
  }

  protected void run(String label, Work work) {
    if (Boolean.TRUE.equals(busy.getValue())) return;
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
                      if (cleared) return;
                      result.run();
                      busy.setValue(false);
                    });
          } catch (Exception e) {
            AppLogger.failure(label, e);
            new Handler(Looper.getMainLooper())
                .post(
                    () -> {
                      if (cleared) return;
                      busy.setValue(false);
                      status.setValue("Action did not complete");
                      error.setValue(
                          e instanceof java.io.IOException
                              ? e.getMessage()
                              : "Unable to complete action");
                    });
          }
        });
  }

  public void retry() {
    if (retry != null) retry.run();
  }

  protected void clearRetry() {
    retry = null;
  }

  @Override
  protected void onCleared() {
    cleared = true;
    retry = null;
  }
}
