package com.company.pda.infrastructure.security;

import org.springframework.security.core.AuthenticationException;

final class JwtAuthenticationException extends AuthenticationException {
  private final String code;

  JwtAuthenticationException(String code) {
    super(code);
    this.code = code;
  }

  String code() {
    return code;
  }
}
