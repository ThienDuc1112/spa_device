package com.company.application;

import com.company.domain.*;
import com.company.domain.Models.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentActor {
  private final AuthRepository users;

  public CurrentActor(AuthRepository users) {
    this.users = users;
  }

  public Actor get() {
    var jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var user = users.userById(Long.parseLong(jwt.getSubject()));
    if (user == null
        || !user.active()
        || user.storeId() != ((Number) jwt.getClaim("storeId")).longValue())
      throw new BusinessException(401, "Session no longer valid");
    return new Actor(user.id(), user.storeId());
  }
}
