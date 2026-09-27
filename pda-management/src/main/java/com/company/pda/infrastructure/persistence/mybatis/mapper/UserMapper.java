package com.company.pda.infrastructure.persistence.mybatis.mapper;

import com.company.pda.infrastructure.persistence.mybatis.entity.RefreshEntity;
import com.company.pda.infrastructure.persistence.mybatis.entity.UserEntity;
import java.util.UUID;

public interface UserMapper {
  long createUser(
      String username, String passwordHash, String fullName, String email, long storeId);

  int assignRole(long userId, String role);

  UserEntity user(String username);

  UserEntity userById(long id);

  java.util.List<String> roles(long id);

  RefreshEntity refresh(UUID id);

  int saveRefresh(RefreshEntity token);

  int consume(UUID id);

  int revoke(UUID familyId);
}
