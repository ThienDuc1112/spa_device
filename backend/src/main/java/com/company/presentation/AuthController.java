package com.company.presentation;

import com.company.application.AuthUseCase;
import com.company.application.dto.Contracts.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthUseCase auth;

  public AuthController(AuthUseCase auth) {
    this.auth = auth;
  }

  @PostMapping("/login")
  public Tokens login(@Valid @RequestBody Login body) {
    return auth.login(body);
  }

  @PostMapping("/refresh")
  public Tokens refresh(@Valid @RequestBody RefreshBody body) {
    return auth.refresh(body);
  }
}
