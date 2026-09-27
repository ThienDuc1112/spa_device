package com.company.pda.infrastructure.persistence.mybatis.converter;

import com.company.pda.domain.auth.model.User;
import com.company.pda.infrastructure.persistence.mybatis.entity.UserEntity;

public final class UserEntityMapper {
  private UserEntityMapper() {}

  public static User toDomain(UserEntity row) {
    return row == null
        ? null
        : new User(row.id(), row.username(), row.passwordHash(), row.storeId(), row.active());
  }

  public static UserEntity toEntity(User model) {
    return model == null
        ? null
        : new UserEntity(
            model.id(), model.username(), model.passwordHash(), model.storeId(), model.active());
  }
}
