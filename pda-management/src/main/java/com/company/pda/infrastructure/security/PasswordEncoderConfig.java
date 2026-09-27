package com.company.pda.infrastructure.security;

import com.company.pda.application.port.out.PasswordHasher;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
  @Bean
  PasswordEncoder passwords() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  PasswordHasher passwordHasher(PasswordEncoder encoder) {
    return new PasswordHasher() {
      public String encode(String value) {
        return encoder.encode(value);
      }

      public boolean matches(String raw, String encoded) {
        return encoder.matches(raw, encoded);
      }
    };
  }
}
