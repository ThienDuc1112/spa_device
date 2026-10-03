package com.company.pda.presentation.rest.auth;

import com.company.pda.application.auth.usecase.AuthUseCase;
import com.company.pda.application.auth.usecase.RegisterUserUseCase;
import com.company.pda.presentation.rest.auth.dto.LoginRequest;
import com.company.pda.presentation.rest.auth.dto.LoginResponse;
import com.company.pda.presentation.rest.auth.dto.RefreshTokenRequest;
import com.company.pda.presentation.rest.auth.dto.RegisterUserRequest;
import com.company.pda.presentation.rest.auth.dto.RegisterUserResponse;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthUseCase auth;
  private final RegisterUserUseCase registration;

  public AuthController(AuthUseCase auth, RegisterUserUseCase registration) {
    this.auth = auth;
    this.registration = registration;
  }

  @PostMapping("/register")
  @PreAuthorize("hasRole('MANAGER')")
  @ResponseStatus(HttpStatus.CREATED)
  public RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest body) {
    return RegisterUserResponse.from(registration.register(body.toCommand()));
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest body) {
    return LoginResponse.from(auth.login(body.toCommand()));
  }

  @PostMapping("/refresh")
  public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest body) {
    return LoginResponse.from(auth.refresh(body.toCommand()));
  }
}
