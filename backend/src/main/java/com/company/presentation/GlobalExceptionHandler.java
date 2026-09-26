package com.company.presentation;

import com.company.domain.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  ProblemDetail business(BusinessException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(e.status), e.getMessage());
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    org.springframework.web.bind.ServletRequestBindingException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    jakarta.validation.ConstraintViolationException.class
  })
  ProblemDetail validation(Exception e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request fields");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ProblemDetail conflict(Exception e) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, "Duplicate request or inconsistent data");
  }

  @ExceptionHandler(AccessDeniedException.class)
  ProblemDetail forbidden(Exception e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Permission denied");
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail unexpected(Exception e) {
    String ref = java.util.UUID.randomUUID().toString();
    log.error("Unhandled failure {}", ref, e);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "Internal error; reference " + ref);
  }
}
