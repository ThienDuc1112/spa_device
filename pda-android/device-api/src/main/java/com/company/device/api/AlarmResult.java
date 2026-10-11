package com.company.device.api;

public final class AlarmResult {
  private final AlarmStatus status;
  private final String message;

  public AlarmResult(AlarmStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public AlarmStatus status() {
    return status;
  }

  public String message() {
    return message;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof AlarmResult other)) return false;
    return java.util.Objects.equals(status, other.status)
        && java.util.Objects.equals(message, other.message);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(status, message);
  }

  @Override
  public String toString() {
    return "AlarmResult[status=" + status + ", message=" + message + "]";
  }

  public boolean ringing() {
    return status == AlarmStatus.RINGING;
  }
}
