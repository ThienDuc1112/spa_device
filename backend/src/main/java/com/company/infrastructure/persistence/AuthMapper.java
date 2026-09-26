package com.company.infrastructure.persistence;

import com.company.domain.AuthRepository;
import com.company.domain.Models.*;
import java.util.UUID;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AuthMapper extends AuthRepository {
  @Select("SELECT id,username,password_hash,store_id,active FROM users WHERE username=#{username}")
  User user(String username);

  @Select("SELECT id,username,password_hash,store_id,active FROM users WHERE id=#{id}")
  User userById(long id);

  @Select(
      "SELECT role_name FROM roles r JOIN user_roles ur ON ur.role_id=r.id WHERE ur.user_id=#{id}")
  java.util.List<String> roles(long id);

  @Select(
      "SELECT id,family_id,user_id,token_hash,expires_at,consumed_at,revoked FROM refresh_tokens"
          + " WHERE id=#{id} FOR UPDATE")
  Refresh refresh(UUID id);

  @Insert(
      "INSERT INTO refresh_tokens(id,family_id,user_id,token_hash,expires_at)"
          + " VALUES(#{id},#{familyId},#{userId},#{tokenHash},#{expiresAt})")
  int saveRefresh(Refresh token);

  @Update(
      "UPDATE refresh_tokens SET consumed_at=now() WHERE id=#{id} AND consumed_at IS NULL AND"
          + " revoked=false")
  int consume(UUID id);

  @Update("UPDATE refresh_tokens SET revoked=true WHERE family_id=#{familyId}")
  int revoke(UUID familyId);
}
