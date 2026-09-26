package com.company.pda.data.remote.dto.pdafinder;

public final class PdaFinderDto {
  public static class Device {
    public long id;
    public String deviceCode, deviceName, lastActiveAt;
    public boolean reachable;
  }

  public static class Request {
    public String id, status, expiresAt;
    public long deviceId;
  }

  public record Find(long deviceId) {}

  public record Stop(String requestId) {}

  public record Event(String requestId, String status) {}

  public record Register(String deviceCode, String deviceName, String fcmToken) {}

  public static class Registration {
    public long deviceId;
    public String deviceSecret;
  }

  public record Token(String fcmToken) {}
}
