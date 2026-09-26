package com.company.pda.domain.usecase;

import com.company.pda.domain.repository.AuthRepository;

public final class LoginUseCase {
  private final AuthRepository repository;

  public LoginUseCase(AuthRepository repository) {
    this.repository = repository;
  }

  public void execute(String username, String password) throws java.io.IOException {
    repository.login(username, password);
  }
}
