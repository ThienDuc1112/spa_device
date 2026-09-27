package com.company.pda.infrastructure.firebase;

import com.company.pda.application.port.out.NotificationPort;
import com.google.firebase.*;
import com.google.firebase.messaging.*;
import java.time.*;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FirebaseNotificationAdapter implements NotificationPort {
  private final FirebaseMessaging messaging;

  public FirebaseNotificationAdapter(
      org.springframework.beans.factory.ObjectProvider<FirebaseMessaging> messaging) {
    this.messaging = messaging.getIfAvailable();
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
