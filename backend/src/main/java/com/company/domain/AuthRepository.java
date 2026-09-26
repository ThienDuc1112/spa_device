package com.company.domain;

import com.company.domain.Models.*;
import java.util.UUID;

public interface AuthRepository {
  User user(String username);

  User userById(long id);

  java.util.List<String> roles(long id);

  Refresh refresh(UUID id);

  int saveRefresh(Refresh token);

  int consume(UUID id);

  int revoke(UUID familyId);
}
