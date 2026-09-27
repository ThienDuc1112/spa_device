package com.company.pda.presentation.exception;

import org.springframework.http.ProblemDetail;

public final class ErrorResponse extends ProblemDetail {
  private ErrorResponse(int status, String detail) {
    super(status);
    setDetail(detail);
  }

  public static ErrorResponse of(int status, String detail) {
    return new ErrorResponse(status, detail);
  }
}
