package com.company.pda.application.device.dto;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class UpdateFcmTokenCommand {
  private final String fcmToken;

  @java.beans.ConstructorProperties({"fcmToken"})
  public UpdateFcmTokenCommand(String fcmToken) {
    this.fcmToken = fcmToken;
  }

  public String fcmToken() {
    return fcmToken;
  }

  public String getFcmToken() {
    return fcmToken;
  }
}
