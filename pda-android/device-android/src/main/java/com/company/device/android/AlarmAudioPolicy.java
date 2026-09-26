package com.company.device.android;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

/** Checks the effective DND policy without changing the user's global DND settings. */
public final class AlarmAudioPolicy {
  private AlarmAudioPolicy() {}

  public static boolean allowsAlarm(int filter, boolean priorityAllowsAlarms) {
    return filter == NotificationManager.INTERRUPTION_FILTER_ALL
        || filter == NotificationManager.INTERRUPTION_FILTER_ALARMS
        || (filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY && priorityAllowsAlarms);
  }

  public static String blockingReason(Context context) {
    var manager = context.getSystemService(NotificationManager.class);
    int filter = manager.getCurrentInterruptionFilter();
    boolean allowed = false;
    if (filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
      if (!manager.isNotificationPolicyAccessGranted())
        return "Grant Do Not Disturb access so this PDA can check whether alarms are allowed.";
      try {
        var policy =
            Build.VERSION.SDK_INT >= 30
                ? manager.getConsolidatedNotificationPolicy()
                : manager.getNotificationPolicy();
        allowed =
            policy != null
                && (policy.priorityCategories & NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS)
                    != 0;
      } catch (SecurityException e) {
        return "Do Not Disturb access was revoked. Check finder sound settings.";
      }
    }
    return allowsAlarm(filter, allowed)
        ? null
        : "Do Not Disturb blocks alarms. Allow alarms in the active mode, or turn that mode off.";
  }
}
