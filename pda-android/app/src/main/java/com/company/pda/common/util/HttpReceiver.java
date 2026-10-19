package com.company.pda.common.util;

/** Result callback delivered on the main thread while the screen is started. */
@FunctionalInterface
public interface HttpReceiver<T> {
  void onSuccess(T result);

  /** Return true if this callback handled the error; false uses the screen's error UI. */
  default boolean onError(Exception error) {
    return false;
  }
}
