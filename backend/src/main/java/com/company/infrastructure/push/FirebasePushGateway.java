package com.company.infrastructure.push;

import com.company.domain.PushGateway;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.*;
import com.google.firebase.messaging.*;
import java.time.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FirebasePushGateway implements PushGateway {
  private final FirebaseMessaging messaging;

  public FirebasePushGateway(@Value("${app.fcm-enabled}") boolean enabled)
      throws java.io.IOException {
    messaging =
        enabled
            ? FirebaseMessaging.getInstance(
                FirebaseApp.initializeApp(
                    FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .setConnectTimeout(5000)
                        .setReadTimeout(5000)
                        .build()))
            : null;
  }

  public void send(String token, String command, UUID id, Instant expiresAt) {
    if (messaging == null) throw new IllegalStateException("FCM disabled");
    long ttl = Math.max(0, Duration.between(Instant.now(), expiresAt).toMillis());
    try {
      messaging.send(
          Message.builder()
              .setToken(token)
              .putData("command", command)
              .putData("requestId", id.toString())
              .putData("expiresAt", expiresAt.toString())
              .setAndroidConfig(
                  AndroidConfig.builder()
                      .setPriority(AndroidConfig.Priority.HIGH)
                      .setTtl(ttl)
                      .build())
              .build());
    } catch (FirebaseMessagingException e) {
      if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) throw new InvalidToken();
      throw new IllegalStateException("Push delivery failed: " + e.getMessagingErrorCode());
    }
  }
}
