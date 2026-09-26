package com.company.pda.common.util;

import android.content.Intent;
import android.widget.ProgressBar;
import androidx.appcompat.app.*;
import androidx.core.view.*;
import androidx.lifecycle.*;
import com.company.pda.PdaApplication;
import com.company.pda.presentation.auth.LoginActivity;

public final class ScreenSupport {
  public static void insets(android.view.View view) {
    ViewCompat.setOnApplyWindowInsetsListener(
        view,
        (v, insets) -> {
          var bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
          return insets;
        });
  }

  public static void observe(AppCompatActivity activity, LifecycleOwner owner, AsyncViewModel vm) {
    var loading =
        new AlertDialog.Builder(activity)
            .setMessage("Working…")
            .setView(new ProgressBar(activity))
            .setCancelable(false)
            .create();
    final AlertDialog[] errorDialog = {null};
    vm.busy.observe(
        owner,
        b -> {
          if (Boolean.TRUE.equals(b)) loading.show();
          else loading.dismiss();
        });
    vm.error.observe(
        owner,
        error -> {
          if (error == null) return;
          vm.error.setValue(null);
          if (!(activity instanceof LoginActivity)
              && !((PdaApplication) activity.getApplication()).modules().auth.loggedIn()) {
            login(activity);
            return;
          }
          errorDialog[0] =
              new AlertDialog.Builder(activity)
                  .setTitle("Unable to complete action")
                  .setMessage(error)
                  .setNegativeButton("Close", null)
                  .setPositiveButton("Retry", (d, w) -> vm.retry())
                  .show();
        });
    owner
        .getLifecycle()
        .addObserver(
            new DefaultLifecycleObserver() {
              public void onDestroy(LifecycleOwner o) {
                loading.dismiss();
                if (errorDialog[0] != null) errorDialog[0].dismiss();
              }
            });
  }

  public static void login(AppCompatActivity activity) {
    activity.startActivity(
        new Intent(activity, LoginActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    activity.finish();
  }
}
