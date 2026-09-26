package com.company.pda.common.logging;

public final class AppLogger {
  private AppLogger() {}

  public static void failure(String operation, Throwable cause) {
    android.util.Log.w("RetailPda", operation + " failed: " + cause.getClass().getSimpleName());
  }
}
