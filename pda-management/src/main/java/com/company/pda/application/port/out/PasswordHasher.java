package com.company.pda.application.port.out;

public interface PasswordHasher {
  String encode(String value);

  boolean matches(String raw, String encoded);
}
