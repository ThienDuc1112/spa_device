package com.company.pda.infrastructure.security;

import com.company.pda.application.port.out.CurrentActor;
import com.company.pda.domain.auth.model.User;
import com.company.pda.domain.auth.repository.AuthRepository;
import com.company.pda.domain.shared.exception.DomainException;
import com.company.pda.domain.shared.model.Actor;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentActor implements CurrentActor {
  private final AuthRepository users;

  public SecurityCurrentActor(AuthRepository users) {
    this.users = users;
  }

  public Actor get() {
    final Claims jwt =
        (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    final User user = users.userById(Long.parseLong(jwt.getSubject()));
    if (user == null
        || !user.active()
        || user.storeId() != ((Number) jwt.get("storeId")).longValue())
      throw new DomainException(401, "SESSION_REVOKED", "Session no longer valid");
    return new Actor(user.id(), user.storeId());
  }
}
