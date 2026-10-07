package com.company.pda.infrastructure.firebase;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.company.pda.BuildConfig;
import com.company.pda.PdaApplication;
import com.google.firebase.messaging.*;

public class PdaFirebaseMessagingService extends FirebaseMessagingService {
  public void onNewToken(String token) {
    Log.i("FinderFcm", "FCM token refreshed; scheduling registration sync");
    ((PdaApplication) getApplication()).modules().fcm.refreshed(token);
  }

  public void onMessageReceived(RemoteMessage message) {
    Log.i(
        "FinderFcm",
        "message received transport="
            + BuildConfig.FINDER_TRANSPORT
            + " priority="
            + message.getPriority()
            + " originalPriority="
            + message.getOriginalPriority());
    if (!"fcm".equals(BuildConfig.FINDER_TRANSPORT)) {
      Log.i("FinderFcm", "ignored: configured transport is not fcm");
      return;
    }
    var data = message.getData();
    Log.i(
        "FinderFcm",
        "requestId="
            + data.get("requestId")
            + " command="
            + data.get("command")
            + " expiresAt="
            + data.get("expiresAt"));
    try {
      java.util.UUID.fromString(data.get("requestId"));
    } catch (Exception error) {
      Log.w("FinderFcm", "rejected: invalid requestId");
      return;
    }
    if (!"FIND".equals(data.get("command")) && !"STOP".equals(data.get("command"))) {
      Log.w("FinderFcm", "ignored: unsupported command");
      return;
    }
    var fcm = ((PdaApplication) getApplication()).modules().fcm;
    if (message.getPriority() == RemoteMessage.PRIORITY_HIGH) fcm.received();
    else if ("FIND".equals(data.get("command"))) fcm.failed("DELIVERY_ERROR");
    new Handler(Looper.getMainLooper())
        .post(
            () ->
                FinderCommandHandler.handle(
                    (PdaApplication) getApplication(),
                    data.get("requestId"),
                    data.get("command"),
                    data.get("expiresAt"),
                    message.getPriority() == RemoteMessage.PRIORITY_HIGH));
  }
}
