package com.company.pda.infrastructure.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.*;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

@Configuration
public class FirebaseConfig {
  @Bean(destroyMethod = "delete")
  @ConditionalOnProperty(name = "app.fcm-enabled", havingValue = "true")
  FirebaseApp firebaseApp() throws java.io.IOException {
    return FirebaseApp.initializeApp(
        FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.getApplicationDefault())
            .setConnectTimeout(5000)
            .setReadTimeout(5000)
            .build());
  }

  @Bean
  @ConditionalOnProperty(name = "app.fcm-enabled", havingValue = "true")
  FirebaseMessaging firebaseMessaging(FirebaseApp app) {
    return FirebaseMessaging.getInstance(app);
  }
}
