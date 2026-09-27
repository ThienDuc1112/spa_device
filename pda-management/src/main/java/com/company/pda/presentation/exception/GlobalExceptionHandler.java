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
  ErrorResponse business(DomainException e) {
    return ErrorResponse.of(e.status, e.getMessage());
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    org.springframework.web.bind.ServletRequestBindingException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    jakarta.validation.ConstraintViolationException.class
  })
  ErrorResponse validation(Exception e) {
    return ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Invalid request fields");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ErrorResponse conflict(Exception e) {
    return ErrorResponse.of(HttpStatus.CONFLICT.value(), "Duplicate request or inconsistent data");
  }

  @ExceptionHandler(AccessDeniedException.class)
  ErrorResponse forbidden(Exception e) {
    return ErrorResponse.of(HttpStatus.FORBIDDEN.value(), "Permission denied");
  }

  @ExceptionHandler(Exception.class)
  ErrorResponse unexpected(Exception e) {
    String ref = java.util.UUID.randomUUID().toString();
    log.error("Unhandled failure {}", ref, e);
    return ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal error; reference " + ref);
  }
}
