package com.company.pda.infrastructure.security;

import com.company.pda.application.port.out.CurrentActor;
import com.company.pda.domain.auth.repository.AuthRepository;
import com.company.pda.domain.shared.exception.DomainException;
import com.company.pda.domain.shared.model.Actor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentActor implements CurrentActor {
  private final AuthRepository users;

  public SecurityCurrentActor(AuthRepository users) {
    this.users = users;
  }

  public Actor get() {
    var jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var user = users.userById(Long.parseLong(jwt.getSubject()));
    if (user == null
        || !user.active()
        || user.storeId() != ((Number) jwt.getClaim("storeId")).longValue())
      throw new DomainException(401, "Session no longer valid");
    return new Actor(user.id(), user.storeId());
  }
}
