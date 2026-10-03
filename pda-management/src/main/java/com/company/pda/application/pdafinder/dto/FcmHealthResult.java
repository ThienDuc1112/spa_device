package com.company.pda.application.pdafinder.dto;

/** Last observed FCM transport state, not a guarantee that a message reached the PDA. */
@lombok.EqualsAndHashCode
@lombok.ToString
public final class FcmHealthResult {
  private final boolean fallbackRequired;
  private final String reason;

  @java.beans.ConstructorProperties({"fallbackRequired", "reason"})
  public FcmHealthResult(boolean fallbackRequired, String reason) {
    this.fallbackRequired = fallbackRequired;
    this.reason = reason;
  }

  public boolean fallbackRequired() {
    return fallbackRequired;
  }

  public boolean getFallbackRequired() {
    return fallbackRequired;
  }

  public String reason() {
    return reason;
  }

  public String getReason() {
    return reason;
  }
}
