package com.company.pda.data.repository;

import static com.company.pda.common.util.ApiCalls.execute;

import com.company.pda.data.local.preferences.TokenStorage;
import com.company.pda.data.remote.api.AuthApi;
import com.company.pda.data.remote.dto.auth.AuthDto.Login;
import com.company.pda.domain.repository.AuthRepository;

public class AuthRepositoryImpl implements AuthRepository {
  private final AuthApi api;
  private final TokenStorage storage;

  public AuthRepositoryImpl(AuthApi api, TokenStorage storage) {
    this.api = api;
    this.storage = storage;
  }

  public void login(String username, String password) throws java.io.IOException {
    storage.tokens(execute(api.login(new Login(username, password))));
  }

  public boolean loggedIn() {
    return storage.tokens() != null;
  }

  public void logout() {
    storage.logout();
  }
}
