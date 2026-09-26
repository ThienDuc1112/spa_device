package com.company.device.api;

public record AlarmResult(AlarmStatus status, String message) {
  public boolean ringing() {
    return status == AlarmStatus.RINGING;
  }
}
