package com.company.pda.domain.auth.exception;

import com.company.pda.domain.shared.exception.DomainException;

public class InvalidCredentialsException extends DomainException {
  public InvalidCredentialsException() {
    super(401, "Invalid credentials");
  }
}
