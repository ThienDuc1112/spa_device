package com.company.pda.domain.pdafinder.model;

import java.util.UUID;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class PdaAlertLog {
  private final UUID requestId;
  private final long deviceId;
  private final String event;
  private final String message;

  @java.beans.ConstructorProperties({"requestId", "deviceId", "event", "message"})
  public PdaAlertLog(UUID requestId, long deviceId, String event, String message) {
    this.requestId = requestId;
    this.deviceId = deviceId;
    this.event = event;
    this.message = message;
  }

  public UUID requestId() {
    return requestId;
  }

  public UUID getRequestId() {
    return requestId;
  }

  public long deviceId() {
    return deviceId;
  }

  public long getDeviceId() {
    return deviceId;
  }

  public String event() {
    return event;
  }

  public String getEvent() {
    return event;
  }

  public String message() {
    return message;
  }

  public String getMessage() {
    return message;
  }
}
