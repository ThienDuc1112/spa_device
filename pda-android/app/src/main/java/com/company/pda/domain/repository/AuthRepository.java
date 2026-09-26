package com.company.pda.domain.repository;

import com.company.pda.domain.model.*;

public interface AuthRepository {
  void login(String username, String password) throws java.io.IOException;

  boolean loggedIn();

  void logout();
}
