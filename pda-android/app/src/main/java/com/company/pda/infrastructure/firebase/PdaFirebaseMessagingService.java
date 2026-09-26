package com.company.pda.infrastructure.firebase;

import com.company.pda.PdaApplication;
import com.company.pda.domain.usecase.*;
import com.company.pda.infrastructure.alarm.PdaAlarmService;
import com.google.firebase.messaging.*;

public class PdaFirebaseMessagingService extends FirebaseMessagingService {
  public void onNewToken(String token) {
    ((PdaApplication) getApplication()).modules().fcm.refreshed(token);
  }

  public void onMessageReceived(RemoteMessage message) {
    var data = message.getData();
    String id = data.get("requestId"), command = data.get("command");
    if (id == null) return;
    try {
      java.util.UUID.fromString(id);
    } catch (Exception e) {
      return;
    }
    var app = (PdaApplication) getApplication();
    if ("STOP".equals(command)) {
      new StopPdaAlarmUseCase(app.modules().finder).execute(id);
      return;
    }
    if (!"FIND".equals(command) || app.modules().tokens.get("handled:" + id) != null) return;
    long expiry;
    try {
      expiry = java.time.Instant.parse(data.get("expiresAt")).toEpochMilli();
    } catch (Exception e) {
      return;
    }
    if (expiry <= System.currentTimeMillis()) return;
    if (message.getPriority() != RemoteMessage.PRIORITY_HIGH) {
      PdaAlarmService.notifyFallback(this, id);
      SyncWorker.event(app, id, "FAILED");
      return;
    }
    try {
      new StartPdaAlarmUseCase(app.modules().finder).execute(id, expiry);
    } catch (RuntimeException e) {
      PdaAlarmService.notifyFallback(this, id);
      SyncWorker.event(app, id, "FAILED");
    }
  }
}
