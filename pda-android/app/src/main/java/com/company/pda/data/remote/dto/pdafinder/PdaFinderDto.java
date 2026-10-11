package com.company.pda.data.remote.dto.pdafinder;

public final class PdaFinderDto {
  public static class FcmHealth {
    public boolean fallbackRequired;
    public String reason;
  }

  public static class Command {
    public String requestId, command, expiresAt;
  }

  public static class Device {
    public long id;
    public String deviceCode, deviceName, lastActiveAt;
    public boolean reachable;
  }

  public static class Request {
    public String id, status, expiresAt;
    public long deviceId;
  }

  public static final class Find {
    private final long deviceId;

    public Find(long deviceId) {
      this.deviceId = deviceId;
    }

    public long deviceId() {
      return deviceId;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Find other)) return false;
      return java.util.Objects.equals(deviceId, other.deviceId);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(deviceId);
    }

    @Override
    public String toString() {
      return "Find[deviceId=" + deviceId + "]";
    }
  }

  public static final class Stop {
    private final String requestId;

    public Stop(String requestId) {
      this.requestId = requestId;
    }

    public String requestId() {
      return requestId;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Stop other)) return false;
      return java.util.Objects.equals(requestId, other.requestId);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(requestId);
    }

    @Override
    public String toString() {
      return "Stop[requestId=" + requestId + "]";
    }
  }

  public static final class Event {
    private final String requestId;
    private final String status;

    public Event(String requestId, String status) {
      this.requestId = requestId;
      this.status = status;
    }

    public String requestId() {
      return requestId;
    }

    public String status() {
      return status;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Event other)) return false;
      return java.util.Objects.equals(requestId, other.requestId)
          && java.util.Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(requestId, status);
    }

    @Override
    public String toString() {
      return "Event[requestId=" + requestId + ", status=" + status + "]";
    }
  }

  public static final class Register {
    private final String deviceCode;
    private final String deviceName;
    private final String fcmToken;

    public Register(String deviceCode, String deviceName, String fcmToken) {
      this.deviceCode = deviceCode;
      this.deviceName = deviceName;
      this.fcmToken = fcmToken;
    }

    public String deviceCode() {
      return deviceCode;
    }

    public String deviceName() {
      return deviceName;
    }

    public String fcmToken() {
      return fcmToken;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Register other)) return false;
      return java.util.Objects.equals(deviceCode, other.deviceCode)
          && java.util.Objects.equals(deviceName, other.deviceName)
          && java.util.Objects.equals(fcmToken, other.fcmToken);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(deviceCode, deviceName, fcmToken);
    }

    @Override
    public String toString() {
      return "Register[deviceCode="
          + deviceCode
          + ", deviceName="
          + deviceName
          + ", fcmToken="
          + fcmToken
          + "]";
    }
  }

  public static class Registration {
    public long deviceId;
    public String deviceSecret;
  }

  public static final class Token {
    private final String fcmToken;

    public Token(String fcmToken) {
      this.fcmToken = fcmToken;
    }

    public String fcmToken() {
      return fcmToken;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Token other)) return false;
      return java.util.Objects.equals(fcmToken, other.fcmToken);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(fcmToken);
    }

    @Override
    public String toString() {
      return "Token[fcmToken=" + fcmToken + "]";
    }
  }
}
