package com.company.pda.application.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class SessionPolicy {
  public final long accessSeconds, sessionSeconds, warningSeconds;

  public SessionPolicy(
      @Value("${app.auth.access-seconds:900}") long accessSeconds,
      @Value("${app.auth.session-seconds:7200}") long sessionSeconds,
      @Value("${app.auth.warning-seconds:300}") long warningSeconds) {
    if (accessSeconds <= 0
        || sessionSeconds <= accessSeconds
        || warningSeconds <= 0
        || warningSeconds >= sessionSeconds)
      throw new IllegalArgumentException("Invalid token/session durations");
    this.accessSeconds = accessSeconds;
    this.sessionSeconds = sessionSeconds;
    this.warningSeconds = warningSeconds;
  }
}
