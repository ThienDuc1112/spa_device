package com.company.pda.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class ErrorResponse {
  private final int status;
  private final String detail;
  private final String instance;
  private String code;

  @com.fasterxml.jackson.annotation.JsonInclude(
      com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
  public String getCode() {
    return code;
  }

  private ErrorResponse(int status, String detail, String instance) {
    this.status = status;
    this.detail = detail;
    this.instance = instance;
  }

  public String getType() {
    return "about:blank";
  }

  public String getTitle() {
    return HttpStatus.valueOf(status).getReasonPhrase();
  }

  public int getStatus() {
    return status;
  }

  public String getDetail() {
    return detail;
  }

  public String getInstance() {
    return instance;
  }

  public static ResponseEntity<ErrorResponse> of(
      int status, String detail, javax.servlet.http.HttpServletRequest request) {
    return of(status, null, detail, request);
  }

  public static ResponseEntity<ErrorResponse> of(
      int status, String code, String detail, javax.servlet.http.HttpServletRequest request) {
    ErrorResponse body = new ErrorResponse(status, detail, request.getRequestURI());
    body.code = code;
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
  }
}
