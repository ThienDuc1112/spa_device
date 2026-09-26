package com.company.pda.common.exception;

public class ApiException extends java.io.IOException {
  public final int status;

  public ApiException(int status, String message) {
    super(message);
    this.status = status;
  }
}
