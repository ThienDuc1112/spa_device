package com.company.pda.infrastructure.firebase;

import android.os.Handler;
import android.os.Looper;
import com.company.pda.BuildConfig;
import com.company.pda.PdaApplication;
import com.google.firebase.messaging.*;

public class PdaFirebaseMessagingService extends FirebaseMessagingService {
  public void onNewToken(String token) {
    ((PdaApplication) getApplication()).modules().fcm.refreshed(token);
  }

  public void onMessageReceived(RemoteMessage message) {
    if (!"fcm".equals(BuildConfig.FINDER_TRANSPORT)) return;
    var data = message.getData();
    try {
      java.util.UUID.fromString(data.get("requestId"));
    } catch (Exception error) {
      return;
    }
    if (!"FIND".equals(data.get("command")) && !"STOP".equals(data.get("command"))) return;
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
