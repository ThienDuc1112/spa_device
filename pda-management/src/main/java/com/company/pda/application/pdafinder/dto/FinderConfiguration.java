package com.company.pda.application.pdafinder.dto;

/** Safe runtime configuration for troubleshooting delivery, without Firebase credentials. */
public final class FinderConfiguration {
  public final boolean fcmEnabled;
  public final boolean schedulerEnabled;

  public FinderConfiguration(boolean fcmEnabled, boolean schedulerEnabled) {
    this.fcmEnabled = fcmEnabled;
    this.schedulerEnabled = schedulerEnabled;
  }
}
