package com.company.pda.infrastructure.firebase;

import static com.company.pda.common.util.ApiCalls.execute;

import android.content.Context;
import android.util.Log;
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
    Log.d(
        "FinderSync", "enqueue workId=" + request.getId() + " network=CONNECTED backoffSeconds=30");
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
    Log.i(
        "FinderSync",
        "worker start workId="
            + getId()
            + " attempt="
            + getRunAttemptCount()
            + " deviceId="
            + id
            + " secretPresent="
            + (secret != null)
            + " fcmTokenPresent="
            + (token != null));
    if (id == null || secret == null) {
      Log.w("FinderSync", "worker skipped: missing device credentials; queued events retained");
      return Result.success();
    }
    try {
      if (token != null) {
        Log.d("FinderSync", "HTTP send token registration");
        execute(m.finderApi.token(Long.parseLong(id), secret, new PdaFinderDto.Token(token)));
        Log.d("FinderSync", "token registration succeeded");
      }
      for (var e : m.database.events().pending()) {
        try {
          Log.i(
              "FinderSync",
              "HTTP send POST /pda/events requestId="
                  + e.requestId
                  + " status="
                  + e.status
                  + " eventId="
                  + e.id);
          execute(
              m.finderApi.event(
                  Long.parseLong(id), secret, new PdaFinderDto.Event(e.requestId, e.status)));
          Log.i(
              "FinderSync", "event ACK succeeded requestId=" + e.requestId + " status=" + e.status);
        } catch (ApiException error) {
          Log.w(
              "FinderSync",
              "event ACK rejected requestId="
                  + e.requestId
                  + " status="
                  + e.status
                  + " httpStatus="
                  + error.status
                  + " discard="
                  + (error.status == 404 || error.status == 409));
          if (error.status != 404 && error.status != 409) throw error;
        }
        m.database.events().deleteEvent(e.id);
        Log.d("FinderSync", "removed queued eventId=" + e.id + " requestId=" + e.requestId);
      }
      Log.i("FinderSync", "worker completed workId=" + getId());
      return Result.success();
    } catch (ApiException e) {
      Log.w(
          "FinderSync",
          "worker HTTP failure status="
              + e.status
              + " result="
              + (e.status == 401 || e.status == 403 ? "FAILURE; credentials rejected" : "RETRY"));
      return e.status == 401 || e.status == 403 ? Result.failure() : Result.retry();
    } catch (Exception e) {
      Log.e("FinderSync", "worker failed; RETRY workId=" + getId(), e);
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
              try {
                app.modules().database.events().event(event);
                Log.i("FinderSync", "event persisted requestId=" + id + " status=" + status);
                schedule(app);
              } catch (RuntimeException error) {
                Log.e(
                    "FinderSync",
                    "event persistence/scheduling failed requestId=" + id + " status=" + status,
                    error);
                throw error;
              }
            });
  }
}
