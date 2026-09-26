package com.company.device.api;

public interface AudioModeController {
  boolean acquire(Runnable onFocusLost);

  void release();

  default void verify() {}
}
