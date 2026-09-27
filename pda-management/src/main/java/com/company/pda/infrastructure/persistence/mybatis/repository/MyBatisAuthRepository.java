package com.company.pda.infrastructure.persistence.mybatis.repository;

import com.company.pda.domain.auth.model.Refresh;
import com.company.pda.domain.auth.model.User;
import com.company.pda.domain.auth.repository.AuthRepository;
import com.company.pda.infrastructure.persistence.mybatis.converter.RefreshEntityMapper;
import com.company.pda.infrastructure.persistence.mybatis.converter.UserEntityMapper;
import com.company.pda.infrastructure.persistence.mybatis.mapper.UserMapper;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAuthRepository implements AuthRepository {
  private final UserMapper mapper;

  public MyBatisAuthRepository(UserMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public long createUser(
      String username, String passwordHash, String fullName, String email, long storeId) {
    return mapper.createUser(username, passwordHash, fullName, email, storeId);
  }

  @Override
  public int assignRole(long userId, String role) {
    return mapper.assignRole(userId, role);
  }

  @Override
  public User user(String username) {
    return UserEntityMapper.toDomain(mapper.user(username));
  }

  @Override
  public User userById(long id) {
    return UserEntityMapper.toDomain(mapper.userById(id));
  }

  @Override
  public java.util.List<String> roles(long id) {
    return mapper.roles(id);
  }

  @Override
  public Refresh refresh(UUID id) {
    return RefreshEntityMapper.toDomain(mapper.refresh(id));
  }

  @Override
  public int saveRefresh(Refresh token) {
    return mapper.saveRefresh(RefreshEntityMapper.toEntity(token));
  }

  @Override
  public int consume(UUID id) {
    return mapper.consume(id);
  }

  @Override
  public int revoke(UUID familyId) {
    return mapper.revoke(familyId);
  }
}
