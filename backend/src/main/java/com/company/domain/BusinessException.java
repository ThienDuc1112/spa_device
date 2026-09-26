package com.company.domain;

public class BusinessException extends RuntimeException {
  public final int status;

  public BusinessException(int status, String message) {
    super(message);
    this.status = status;
  }

  public static <T> T found(T value) {
    if (value == null) throw new BusinessException(404, "Resource not found");
    return value;
  }

  public static void require(boolean valid, String message) {
    if (!valid) throw new BusinessException(409, message);
  }
}
