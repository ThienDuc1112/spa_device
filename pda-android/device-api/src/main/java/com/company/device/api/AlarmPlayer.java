package com.company.device.api;

public interface AlarmPlayer {
  void play(String soundUri) throws Exception;

  void stop();

  default void verify() {}
}
