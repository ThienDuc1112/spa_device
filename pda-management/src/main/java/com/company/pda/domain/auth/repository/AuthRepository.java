package com.company.pda.domain.auth.repository;

import com.company.pda.domain.auth.model.Refresh;
import com.company.pda.domain.auth.model.User;
import java.util.UUID;

public interface AuthRepository {
  long createUser(
      String username, String passwordHash, String fullName, String email, long storeId);

  int assignRole(long userId, String role);

  User user(String username);

  User userById(long id);

  java.util.List<String> roles(long id);

  Refresh refresh(UUID id);

  int saveRefresh(Refresh token);

  int consume(UUID id);

  int revoke(UUID familyId);
}
