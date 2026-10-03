package com.company.pda.presentation.exception;

import com.company.pda.domain.shared.exception.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(DomainException.class)
  ResponseEntity<ErrorResponse> business(
      DomainException e, javax.servlet.http.HttpServletRequest request) {
    return ErrorResponse.of(e.status, e.getMessage(), request);
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    org.springframework.web.bind.ServletRequestBindingException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    javax.validation.ConstraintViolationException.class
  })
  ResponseEntity<ErrorResponse> validation(
      Exception e, javax.servlet.http.HttpServletRequest request) {
    return ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Invalid request fields", request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ErrorResponse> conflict(
      Exception e, javax.servlet.http.HttpServletRequest request) {
    return ErrorResponse.of(
        HttpStatus.CONFLICT.value(), "Duplicate request or inconsistent data", request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ErrorResponse> forbidden(
      Exception e, javax.servlet.http.HttpServletRequest request) {
    return ErrorResponse.of(HttpStatus.FORBIDDEN.value(), "Permission denied", request);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> unexpected(
      Exception e, javax.servlet.http.HttpServletRequest request) {
    String ref = java.util.UUID.randomUUID().toString();
    log.error("Unhandled failure {}", ref, e);
    return ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal error; reference " + ref, request);
  }
}
