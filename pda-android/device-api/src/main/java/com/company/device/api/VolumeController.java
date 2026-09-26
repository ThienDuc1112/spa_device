package com.company.device.api;

public interface VolumeController {
  void maximize();

  void restore();

  default void verify() {}
}
