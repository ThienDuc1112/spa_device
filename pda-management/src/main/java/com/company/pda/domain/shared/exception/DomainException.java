package com.company.pda.domain.shared.exception;

public class DomainException extends RuntimeException {
  public final int status;
  public final String code;

  public DomainException(int status, String message) {
    this(status, null, message);
  }

  public DomainException(int status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public static <T> T found(T value) {
    if (value == null) throw new DomainException(404, "Resource not found");
    return value;
  }

  public static void require(boolean valid, String message) {
    if (!valid) throw new DomainException(409, message);
  }
}
