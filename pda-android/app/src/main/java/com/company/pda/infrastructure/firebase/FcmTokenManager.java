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
    if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
      failed("NOT_CONFIGURED");
    } else {
      com.google.firebase.messaging.FirebaseMessaging.getInstance()
          .getToken()
          .addOnSuccessListener(this::refreshed)
          .addOnFailureListener(error -> failed("TOKEN_ERROR"));
    }
    SyncWorker.schedule(context);
  }

  public void refreshed(String token) {
    tokens.put("fcmToken", token);
    // Obtaining a token repairs token errors, but does not prove message delivery recovered.
    if (!"DELIVERY_ERROR".equals(tokens.get("fcmFailure"))) tokens.put("fcmFailure", null);
    SyncWorker.schedule(context);
  }

  public boolean unavailable() {
    return tokens.get("fcmToken") == null || tokens.get("fcmFailure") != null;
  }

  public void failed(String reason) {
    tokens.put("fcmFailure", reason);
  }

  public void received() {
    tokens.put("fcmFailure", null);
  }

  public void retryIfNeeded() {
    if ("TOKEN_ERROR".equals(tokens.get("fcmFailure"))) initialize();
  }
}
