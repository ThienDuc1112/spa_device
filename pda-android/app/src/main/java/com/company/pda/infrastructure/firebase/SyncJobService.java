package com.company.pda.infrastructure.firebase;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.company.pda.PdaApplication;
import com.company.pda.common.exception.ApiException;
import com.company.pda.data.local.entity.PendingEvent;
import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

/** Network-constrained, persisted synchronization of the FCM token and Room event queue. */
public class SyncJobService extends JobService {
  static final int JOB_ID = 41001;
  private static final String TAG = "FinderSync";
  private static final Object LOCK = new Object();
  private static Run active;
  private final Handler main = new Handler(Looper.getMainLooper());
  private Run running;

  static JobInfo jobInfo(Context context) {
    return new JobInfo.Builder(JOB_ID, new ComponentName(context, SyncJobService.class))
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .setBackoffCriteria(30_000, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
        .setPersisted(true)
        .build();
  }

  public static void schedule(Context context) {
    synchronized (LOCK) {
      var scheduler = context.getSystemService(JobScheduler.class);
      // Retire platform jobs from the removed WorkManager implementation on upgrade.
      // Pending ACKs remain in retail.db and are drained by this service.
      for (var job : scheduler.getAllPendingJobs()) {
        if (job.getService()
            .getClassName()
            .equals("androidx.work.impl.background.systemjob.SystemJobService"))
          scheduler.cancel(job.getId());
      }
      if (active != null) {
        // Replacing a running job would stop it; request a follow-up instead.
        active.changed = true;
        return;
      }
      int result = scheduler.schedule(jobInfo(context));
      if (result == JobScheduler.RESULT_FAILURE)
        Log.e(TAG, "Job scheduling failed; queued events retained");
      else Log.d(TAG, "job scheduled id=" + JOB_ID + " network=ANY backoffSeconds=30");
    }
  }

  @Override
  public boolean onStartJob(JobParameters params) {
    var run = new Run(params);
    synchronized (LOCK) {
      running = run;
      active = run;
    }
    var app = (PdaApplication) getApplication();
    app.modules()
        .io
        .execute(
            () -> {
              boolean retry = synchronize(app, run);
              main.post(
                  () -> {
                    synchronized (LOCK) {
                      if (run.stopped || running != run) return;
                      running = null;
                      if (active == run) active = null;
                      jobFinished(params, retry || run.changed);
                    }
                  });
            });
    return true;
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    stop(params);
    return true;
  }

  private void stop(JobParameters params) {
    Run run;
    synchronized (LOCK) {
      run = running;
      if (run == null || run.params.getJobId() != params.getJobId()) return;
      running = null;
      if (active == run) active = null;
      run.stopped = true;
    }
    run.cancel();
  }

  @Override
  public void onDestroy() {
    if (running != null) stop(running.params);
    super.onDestroy();
  }

  private boolean synchronize(PdaApplication app, Run run) {
    var m = app.modules();
    String id = m.tokens.get("deviceId"),
        secret = m.tokens.get("deviceSecret"),
        token = m.tokens.get("fcmToken");
    Log.i(TAG, "job start id=" + JOB_ID + " deviceId=" + id);
    if (id == null || secret == null) {
      Log.w(TAG, "Missing device credentials; queued events retained");
      return false;
    }
    try {
      if (token != null)
        run.await(
            () -> m.finderApi.token(Long.parseLong(id), secret, new PdaFinderDto.Token(token)));
      while (!run.stopped) {
        var events = m.database.events().pending();
        if (events.isEmpty()) return false;
        for (var event : events) {
          if (run.stopped) return true;
          try {
            run.await(
                () ->
                    m.finderApi.event(
                        Long.parseLong(id),
                        secret,
                        new PdaFinderDto.Event(event.requestId, event.status)));
            Log.i(
                TAG,
                "event ACK succeeded requestId=" + event.requestId + " status=" + event.status);
          } catch (ApiException error) {
            if (error.status != 404 && error.status != 409) throw error;
            Log.w(TAG, "Discarding terminal event id=" + event.id + " httpStatus=" + error.status);
          }
          if (run.stopped) return true;
          m.database.events().deleteEvent(event.id);
        }
      }
      return true;
    } catch (ApiException error) {
      boolean retry = error.status != 401 && error.status != 403;
      Log.w(TAG, "job HTTP failure status=" + error.status + " retry=" + retry);
      return retry;
    } catch (Exception error) {
      if (!run.stopped) Log.e(TAG, "job failed; retrying", error);
      return true;
    }
  }

  private static final class Run {
    final JobParameters params;
    volatile boolean stopped;
    boolean changed; // Guarded by LOCK.
    private CompletableFuture<?> request;

    Run(JobParameters params) {
      this.params = params;
    }

    synchronized void cancel() {
      if (request != null) request.cancel(true);
    }

    <T> T await(Supplier<CompletableFuture<T>> work) throws IOException {
      CompletableFuture<T> call;
      synchronized (this) {
        if (stopped) throw new IOException("Sync job stopped");
        call = work.get();
        request = call;
      }
      try {
        // Called only from the application's IO executor.
        return call.join();
      } catch (CompletionException error) {
        if (error.getCause() instanceof IOException cause) throw cause;
        throw error;
      } finally {
        synchronized (this) {
          request = null;
        }
      }
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
                Log.i(TAG, "event persisted requestId=" + id + " status=" + status);
                schedule(app);
              } catch (RuntimeException error) {
                Log.e(TAG, "event persistence/scheduling failed requestId=" + id, error);
                throw error;
              }
            });
  }
}
