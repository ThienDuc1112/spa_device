package com.company.pda.common.util;

import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.company.pda.PdaApplication;
import java.util.Locale;

/** Common HTTP result entry point, called by ScreenTasks on the main thread. */
public abstract class BaseActivity extends AppCompatActivity {
  private final Handler sessionHandler = new Handler(Looper.getMainLooper());
  private AlertDialog sessionDialog;
  private long warnedExpiry;
  private boolean renewing;
  private boolean endingSession;
  private ScreenTasks sessionTasks;
  private final Runnable sessionTick =
      new Runnable() {
        public void run() {
          updateSession();
          if (!endingSession) sessionHandler.postDelayed(this, 1000);
        }
      };

  protected boolean requiresSession() {
    return false;
  }

  protected void onSessionTimeChanged(String text) {}

  @Override
  protected void onResume() {
    super.onResume();
    if (requiresSession()) {
      if (sessionTasks == null) sessionTasks = new ScreenTasks(this, this);
      sessionHandler.post(sessionTick);
    }
  }

  @Override
  protected void onPause() {
    sessionHandler.removeCallbacks(sessionTick);
    if (sessionDialog != null) {
      sessionDialog.dismiss();
      sessionDialog = null;
      warnedExpiry = 0;
    }
    super.onPause();
  }

  private void updateSession() {
    var tokens = ((PdaApplication) getApplication()).modules().tokens.tokens();
    if (tokens == null) {
      endSession();
      return;
    }
    long expiry = SessionTime.sessionExpiry(tokens);
    if (expiry == 0) {
      onSessionTimeChanged("Session active");
      return;
    }
    long remaining = expiry - SessionTime.now(tokens, System.currentTimeMillis());
    if (remaining <= 0) {
      endSession();
      return;
    }
    long seconds = (remaining + 999) / 1000;
    onSessionTimeChanged(
        String.format(
            Locale.US,
            "Session: %02d:%02d:%02d",
            seconds / 3600,
            (seconds / 60) % 60,
            seconds % 60));
    long warning = tokens.warningSeconds > 0 ? tokens.warningSeconds : 300;
    if (remaining <= warning * 1000 && warnedExpiry != expiry && !renewing) {
      warnedExpiry = expiry;
      sessionDialog =
          new AlertDialog.Builder(this)
              .setTitle("Session expiring")
              .setMessage("Your session is about to expire. Extend it to continue working.")
              .setNegativeButton("Later", (dialog, which) -> sessionDialog = null)
              .setPositiveButton(
                  "Extend session",
                  (dialog, which) -> {
                    sessionDialog = null;
                    renewSession();
                  })
              .setCancelable(false)
              .show();
    }
  }

  protected final void renewSession() {
    if (renewing || endingSession) return;
    renewing = true;
    var modules = ((PdaApplication) getApplication()).modules();
    sessionTasks.runAsync(
        "Extending session...",
        () -> modules.network.extendSession(modules.io),
        new HttpReceiver<com.company.pda.data.remote.dto.auth.AuthDto.Tokens>() {
          public void onSuccess(com.company.pda.data.remote.dto.auth.AuthDto.Tokens tokens) {
            renewing = false;
            warnedExpiry = 0;
            sessionTasks.clearRetry();
            updateSession();
          }

          public boolean onError(Exception error) {
            renewing = false;
            sessionDialog =
                new AlertDialog.Builder(BaseActivity.this)
                    .setTitle("Unable to extend session")
                    .setMessage("Check your connection and try again before the session expires.")
                    .setNegativeButton("Close", null)
                    .setPositiveButton("Retry", (dialog, which) -> renewSession())
                    .show();
            return true;
          }
        });
  }

  private void endSession() {
    if (endingSession) return;
    endingSession = true;
    ((PdaApplication) getApplication()).modules().tokens.logout();
    ScreenSupport.login(this, "Your session has expired or was revoked. Please sign in again.");
  }

  public <T> boolean httpReceiver(HttpReceiver<T> receiver, T result, Exception error) {
    if (requiresSession()
        && error != null
        && ((PdaApplication) getApplication()).modules().tokens.tokens() == null) {
      endSession();
      return true;
    }
    if (error != null) return receiver.onError(error);
    receiver.onSuccess(result);
    return true;
  }
}
