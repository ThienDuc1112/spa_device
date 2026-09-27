package com.company.pda.application.auth.service;

import com.company.pda.application.auth.dto.LoginCommand;
import com.company.pda.application.auth.dto.RefreshTokenCommand;
import com.company.pda.application.auth.dto.TokenResult;
import com.company.pda.application.auth.usecase.AuthUseCase;
import com.company.pda.application.port.out.AuditLogPort;
import com.company.pda.application.port.out.PasswordHasher;
import com.company.pda.application.port.out.TokenProvider;
import com.company.pda.domain.auth.exception.InvalidCredentialsException;
import com.company.pda.domain.auth.model.Refresh;
import com.company.pda.domain.auth.model.User;
import com.company.pda.domain.auth.repository.AuthRepository;
import com.company.pda.domain.shared.exception.DomainException;
import com.company.pda.domain.shared.model.SecretHash;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService implements AuthUseCase {
  private final AuthRepository repo;
  private final PasswordHasher passwords;
  private final TokenProvider tokens;
  private final String dummy;
  private final AuditLogPort ops;

  public AuthenticationService(
      AuthRepository repo, PasswordHasher passwords, TokenProvider tokens, AuditLogPort ops) {
    this.ops = ops;
    this.repo = repo;
    this.passwords = passwords;
    this.tokens = tokens;
    dummy = passwords.encode(UUID.randomUUID().toString());
  }

  @Transactional
  public TokenResult login(LoginCommand body) {
    var user = repo.user(body.username());
    boolean valid = passwords.matches(body.password(), user == null ? dummy : user.passwordHash());
    if (!valid || user == null || !user.active()) throw new InvalidCredentialsException();
    ops.audit(user.id(), user.storeId(), "LOGIN", Long.toString(user.id()));
    return issue(user, UUID.randomUUID());
  }

  @Transactional(noRollbackFor = DomainException.class)
  public TokenResult refresh(RefreshTokenCommand body) {
    var token = repo.refresh(tokens.verifyRefresh(body.refreshToken()));
    if (token == null
        || !MessageDigest.isEqual(
            token.tokenHash().getBytes(StandardCharsets.UTF_8),
            SecretHash.hash(body.refreshToken()).getBytes(StandardCharsets.UTF_8)))
      throw new DomainException(401, "Invalid refresh token");
    if (token.revoked()
        || token.consumedAt() != null
        || token.expiresAt().isBefore(Instant.now())) {
      repo.revoke(token.familyId());
      throw new DomainException(401, "Refresh session revoked; sign in again");
    }
    var user = repo.userById(token.userId());
    if (user == null || !user.active()) {
      repo.revoke(token.familyId());
      throw new DomainException(401, "Account inactive");
    }
    repo.consume(token.id());
    ops.audit(user.id(), user.storeId(), "TOKEN_REFRESH", token.familyId().toString());
    return issue(user, token.familyId());
  }

  private TokenResult issue(User user, UUID family) {
    Instant now = Instant.now();
    UUID id = UUID.randomUUID();
    String access = tokens.access(user, repo.roles(user.id()), now);
    String refresh = tokens.refresh(user, id, now);
    repo.saveRefresh(
        new Refresh(
            id,
            family,
            user.id(),
            SecretHash.hash(refresh),
            now.plus(Duration.ofDays(14)),
            null,
            false));
    return new TokenResult(access, refresh, 900);
  }
}
