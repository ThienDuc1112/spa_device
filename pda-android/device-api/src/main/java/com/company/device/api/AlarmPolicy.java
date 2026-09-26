package com.company.device.api;

public record AlarmPolicy(String soundUri, long expiresAtMillis, boolean maximizeVolume) {
  public long remainingMillis(long now) {
    return Math.max(0, Math.min(300000, expiresAtMillis - now));
  }
}
