package com.company.pda.common.util;

import android.content.*;
import androidx.appcompat.app.*;
import androidx.core.content.ContextCompat;
import com.company.pda.infrastructure.alarm.PdaAlarmService;

public abstract class AlarmAwareActivity extends AppCompatActivity {
  private AlertDialog dialog;
  private final BroadcastReceiver receiver =
      new BroadcastReceiver() {
        public void onReceive(Context c, Intent i) {
          showAlarm();
        }
      };

  protected void onResume() {
    super.onResume();
    ContextCompat.registerReceiver(
        this,
        receiver,
        new IntentFilter(PdaAlarmService.SHOW),
        ContextCompat.RECEIVER_NOT_EXPORTED);
    showAlarm();
  }

  protected void onPause() {
    unregisterReceiver(receiver);
    if (dialog != null) dialog.dismiss();
    super.onPause();
  }

  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    showAlarm();
  }

  private void showAlarm() {
    if (PdaAlarmService.activeId == null || (dialog != null && dialog.isShowing())) return;
    dialog =
        new AlertDialog.Builder(this)
            .setTitle("This PDA is being located")
            .setMessage("An employee is looking for this device.")
            .setPositiveButton(
                "Stop alarm", (d, w) -> stopService(new Intent(this, PdaAlarmService.class)))
            .show();
  }
}
