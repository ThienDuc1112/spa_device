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
  private final SessionPolicy policy;

  public AuthenticationService(
      AuthRepository repo,
      PasswordHasher passwords,
      TokenProvider tokens,
      AuditLogPort ops,
      SessionPolicy policy) {
    this.policy = policy;
    this.ops = ops;
    this.repo = repo;
    this.passwords = passwords;
    this.tokens = tokens;
    dummy = passwords.encode(UUID.randomUUID().toString());
  }

  @Transactional
  public TokenResult login(LoginCommand body) {
    final User user = repo.user(body.username());
    boolean valid = passwords.matches(body.password(), user == null ? dummy : user.passwordHash());
    if (!valid || user == null || !user.active()) throw new InvalidCredentialsException();
    ops.audit(user.id(), user.storeId(), "LOGIN", Long.toString(user.id()));
    return issue(user, UUID.randomUUID(), Instant.now().plusSeconds(policy.sessionSeconds));
  }

  @Transactional(noRollbackFor = DomainException.class)
  public TokenResult refresh(RefreshTokenCommand body) {
    final Refresh token = repo.refresh(tokens.verifyRefresh(body.refreshToken()));
    if (token == null
        || !MessageDigest.isEqual(
            token.tokenHash().getBytes(StandardCharsets.UTF_8),
            SecretHash.hash(body.refreshToken()).getBytes(StandardCharsets.UTF_8)))
      throw new DomainException(401, "REFRESH_TOKEN_INVALID", "Invalid refresh token");
    if (token.revoked()
        || token.consumedAt() != null
        || !token.expiresAt().isAfter(Instant.now())) {
      repo.revoke(token.familyId());
      throw new DomainException(
          401,
          token.revoked() || token.consumedAt() != null ? "SESSION_REVOKED" : "SESSION_EXPIRED",
          "Refresh session expired or revoked; sign in again");
    }
    final User user = repo.userById(token.userId());
    if (user == null || !user.active()) {
      repo.revoke(token.familyId());
      throw new DomainException(401, "SESSION_REVOKED", "Account inactive");
    }
    if (repo.consume(token.id()) != 1) {
      repo.revoke(token.familyId());
      throw new DomainException(401, "SESSION_REVOKED", "Refresh token already used");
    }
    ops.audit(user.id(), user.storeId(), "TOKEN_REFRESH", token.familyId().toString());
    return issue(
        user,
        token.familyId(),
        body.extendSession()
            ? Instant.now().plusSeconds(policy.sessionSeconds)
            : token.expiresAt());
  }

  private TokenResult issue(User user, UUID family, Instant sessionExpiry) {
    Instant now = Instant.now();
    UUID id = UUID.randomUUID();
    Instant accessExpiry = now.plusSeconds(policy.accessSeconds);
    if (accessExpiry.isAfter(sessionExpiry)) accessExpiry = sessionExpiry;
    String access = tokens.access(user, repo.roles(user.id()), now, accessExpiry, family);
    String refresh = tokens.refresh(user, id, now, sessionExpiry);
    repo.saveRefresh(
        new Refresh(id, family, user.id(), SecretHash.hash(refresh), sessionExpiry, null, false));
    TokenResult result =
        new TokenResult(access, refresh, Duration.between(now, accessExpiry).getSeconds());
    result.serverTime = now.toEpochMilli();
    result.accessExpiresAt = accessExpiry.toEpochMilli();
    result.sessionExpiresAt = sessionExpiry.toEpochMilli();
    result.warningSeconds = policy.warningSeconds;
    return result;
  }
}
