package com.company.pda.infrastructure.firebase;

import static com.company.pda.common.util.ApiCalls.execute;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;
import com.company.pda.PdaApplication;
import com.company.pda.common.exception.ApiException;
import com.company.pda.data.local.entity.PendingEvent;
import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto;
import java.util.concurrent.TimeUnit;

public class SyncWorker extends Worker {
  public SyncWorker(Context c, WorkerParameters p) {
    super(c, p);
  }

  public static void schedule(Context context) {
    var request =
        new OneTimeWorkRequest.Builder(SyncWorker.class)
            .setConstraints(
                new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build();
    WorkManager.getInstance(context)
        .enqueueUniqueWork("device-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, request);
  }

  @NonNull
  public Result doWork() {
    var app = (PdaApplication) getApplicationContext();
    var m = app.modules();
    String id = m.tokens.get("deviceId"),
        secret = m.tokens.get("deviceSecret"),
        token = m.tokens.get("fcmToken");
    if (id == null || secret == null) return Result.success();
    try {
      if (token != null)
        execute(m.finderApi.token(Long.parseLong(id), secret, new PdaFinderDto.Token(token)));
      for (var e : m.database.events().pending()) {
        try {
          execute(
              m.finderApi.event(
                  Long.parseLong(id), secret, new PdaFinderDto.Event(e.requestId, e.status)));
        } catch (ApiException error) {
          if (error.status != 404 && error.status != 409) throw error;
        }
        m.database.events().deleteEvent(e.id);
      }
      return Result.success();
    } catch (ApiException e) {
      return e.status == 401 || e.status == 403 ? Result.failure() : Result.retry();
    } catch (Exception e) {
      return Result.retry();
    }
  }

  public static void event(PdaApplication app, String id, String status) {
    app.modules()
        .io
        .execute(
            () -> {
              var event = new PendingEvent();
              event.requestId = id;
              event.status = status;
              app.modules().database.events().event(event);
              schedule(app);
            });
  }
}
