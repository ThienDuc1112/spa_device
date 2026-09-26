package com.company.pda.infrastructure.firebase;

import android.content.Context;
import com.company.pda.data.local.preferences.TokenStorage;

public class FcmTokenManager {
  private final Context context;
  private final TokenStorage tokens;

  public FcmTokenManager(Context context, TokenStorage tokens) {
    this.context = context.getApplicationContext();
    this.tokens = tokens;
  }

  public void initialize() {
    if (!com.google.firebase.FirebaseApp.getApps(context).isEmpty())
      com.google.firebase.messaging.FirebaseMessaging.getInstance()
          .getToken()
          .addOnSuccessListener(this::refreshed);
    SyncWorker.schedule(context);
  }

  public void refreshed(String token) {
    tokens.put("fcmToken", token);
    SyncWorker.schedule(context);
  }
}
