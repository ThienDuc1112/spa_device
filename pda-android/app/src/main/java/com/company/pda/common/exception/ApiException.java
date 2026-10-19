package com.company.pda.common.exception;

public class ApiException extends java.io.IOException {
  public final int status;
  public final String code;

  public ApiException(int status, String message) {
    this(status, null, message);
  }

  public ApiException(int status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}
