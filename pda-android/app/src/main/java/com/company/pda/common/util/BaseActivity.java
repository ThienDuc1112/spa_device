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
  private SessionBubble sessionBubble;
  private boolean sessionError;
  private String remainingTime = "Active";
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

  @Override
  protected void onPostResume() {
    super.onPostResume();
    if (requiresSession()) {
      if (sessionTasks == null) sessionTasks = new ScreenTasks(this, this);
      sessionHandler.removeCallbacks(sessionTick);
      updateSession();
      if (!endingSession) sessionHandler.postDelayed(sessionTick, 1000);
    }
  }

  @Override
  protected void onPause() {
    sessionHandler.removeCallbacks(sessionTick);
    if (sessionBubble != null) {
      sessionBubble.detach();
      sessionBubble = null;
    }
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
      remainingTime = "Active";
      renderSession(false);
      return;
    }
    long remaining = expiry - SessionTime.now(tokens, System.currentTimeMillis());
    if (remaining <= 0) {
      endSession();
      return;
    }
    long seconds = (remaining + 999) / 1000;
    remainingTime =
        String.format(
            Locale.US, "%02d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60);
    long warning = tokens.warningSeconds > 0 ? tokens.warningSeconds : 300;
    boolean expiring = remaining <= warning * 1000;
    renderSession(expiring);
    if (expiring && warnedExpiry != expiry && !renewing && sessionDialog == null) {
      warnedExpiry = expiry;
      showSessionDialog(true);
    }
  }

  private void renderSession(boolean warning) {
    if (sessionBubble == null)
      sessionBubble = new SessionBubble(this, () -> showSessionDialog(false));
    sessionBubble.update(remainingTime, warning, renewing);
    if (sessionDialog != null && sessionDialog.isShowing() && !sessionError) {
      sessionDialog.setMessage(sessionMessage());
      sessionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!renewing);
    }
  }

  private String sessionMessage() {
    return "Time remaining: "
        + remainingTime
        + "\nExtend your session to continue working beyond its current expiry.";
  }

  private void showSessionDialog(boolean warning) {
    if (endingSession || isFinishing() || (sessionDialog != null && sessionDialog.isShowing()))
      return;
    sessionError = false;
    sessionDialog =
        new AlertDialog.Builder(this)
            .setTitle(warning ? "Session expiring" : "Your session")
            .setMessage(sessionMessage())
            .setNegativeButton(warning ? "Later" : "Close", null)
            .setPositiveButton("Extend session", (dialog, which) -> renewSession())
            .setOnDismissListener(dialog -> sessionDialog = null)
            .show();
    sessionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!renewing);
  }

  protected final void renewSession() {
    if (renewing || endingSession) return;
    renewing = true;
    updateSession();
    if (endingSession) {
      renewing = false;
      return;
    }
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
            if (sessionDialog != null) sessionDialog.dismiss();
            sessionError = true;
            sessionDialog =
                new AlertDialog.Builder(BaseActivity.this)
                    .setTitle("Unable to extend session")
                    .setMessage("Check your connection and try again before the session expires.")
                    .setOnDismissListener(dialog -> sessionDialog = null)
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
    sessionHandler.removeCallbacks(sessionTick);
    if (sessionBubble != null) {
      sessionBubble.detach();
      sessionBubble = null;
    }
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
