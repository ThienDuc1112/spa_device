package com.company.device.api;

public final class AlarmPolicy {
  private final String soundUri;
  private final long expiresAtMillis;
  private final boolean maximizeVolume;

  public AlarmPolicy(String soundUri, long expiresAtMillis, boolean maximizeVolume) {
    this.soundUri = soundUri;
    this.expiresAtMillis = expiresAtMillis;
    this.maximizeVolume = maximizeVolume;
  }

  public String soundUri() {
    return soundUri;
  }

  public long expiresAtMillis() {
    return expiresAtMillis;
  }

  public boolean maximizeVolume() {
    return maximizeVolume;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof AlarmPolicy other)) return false;
    return java.util.Objects.equals(soundUri, other.soundUri)
        && java.util.Objects.equals(expiresAtMillis, other.expiresAtMillis)
        && java.util.Objects.equals(maximizeVolume, other.maximizeVolume);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(soundUri, expiresAtMillis, maximizeVolume);
  }

  @Override
  public String toString() {
    return "AlarmPolicy[soundUri="
        + soundUri
        + ", expiresAtMillis="
        + expiresAtMillis
        + ", maximizeVolume="
        + maximizeVolume
        + "]";
  }

  public long remainingMillis(long now) {
    return Math.max(0, Math.min(300000, expiresAtMillis - now));
  }
}
