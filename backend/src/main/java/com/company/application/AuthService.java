package com.company.application;

import com.company.application.dto.Contracts.*;
import com.company.domain.*;
import com.company.domain.Models.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements AuthUseCase {
  private final AuthRepository repo;
  private final PasswordEncoder passwords;
  private final JwtEncoder encoder;
  private final JwtDecoder refreshDecoder;
  private final String issuer;
  private final String dummy;
  private final OperationsRepository ops;

  public AuthService(
      AuthRepository repo,
      PasswordEncoder passwords,
      JwtEncoder encoder,
      SecretKeySpec key,
      OperationsRepository ops,
      @Value("${app.issuer}") String issuer) {
    this.ops = ops;
    this.repo = repo;
    this.passwords = passwords;
    this.encoder = encoder;
    this.issuer = issuer;
    dummy = passwords.encode(UUID.randomUUID().toString());
    var decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
    this.refreshDecoder = decoder;
  }

  @Transactional
  public Tokens login(Login body) {
    var user = repo.user(body.username());
    boolean valid = passwords.matches(body.password(), user == null ? dummy : user.passwordHash());
    if (!valid || user == null || !user.active())
      throw new BusinessException(401, "Invalid credentials");
    ops.audit(user.id(), user.storeId(), "LOGIN", Long.toString(user.id()));
    return issue(user, UUID.randomUUID());
  }

  @Transactional(noRollbackFor = BusinessException.class)
  public Tokens refresh(RefreshBody body) {
    Jwt jwt;
    try {
      jwt = refreshDecoder.decode(body.refreshToken());
    } catch (JwtException e) {
      throw new BusinessException(401, "Invalid refresh token");
    }
    if (!"refresh".equals(jwt.getClaimAsString("type"))
        || !jwt.getAudience().contains("retail-refresh"))
      throw new BusinessException(401, "Invalid refresh token");
    var token = repo.refresh(UUID.fromString(jwt.getId()));
    if (token == null
        || !MessageDigest.isEqual(
            token.tokenHash().getBytes(StandardCharsets.UTF_8),
            hash(body.refreshToken()).getBytes(StandardCharsets.UTF_8)))
      throw new BusinessException(401, "Invalid refresh token");
    if (token.revoked()
        || token.consumedAt() != null
        || token.expiresAt().isBefore(Instant.now())) {
      repo.revoke(token.familyId());
      throw new BusinessException(401, "Refresh session revoked; sign in again");
    }
    var user = repo.userById(token.userId());
    if (user == null || !user.active()) {
      repo.revoke(token.familyId());
      throw new BusinessException(401, "Account inactive");
    }
    repo.consume(token.id());
    ops.audit(user.id(), user.storeId(), "TOKEN_REFRESH", token.familyId().toString());
    return issue(user, token.familyId());
  }

  private Tokens issue(User user, UUID family) {
    Instant now = Instant.now();
    UUID id = UUID.randomUUID();
    String access =
        encode(
            JwtClaimsSet.builder()
                .issuer(issuer)
                .subject("" + user.id())
                .audience(List.of("retail-api"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .claim("type", "access")
                .claim("storeId", user.storeId())
                .claim("roles", repo.roles(user.id()))
                .build());
    String refresh =
        encode(
            JwtClaimsSet.builder()
                .issuer(issuer)
                .subject("" + user.id())
                .audience(List.of("retail-refresh"))
                .id(id.toString())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofDays(14)))
                .claim("type", "refresh")
                .build());
    repo.saveRefresh(
        new Refresh(
            id, family, user.id(), hash(refresh), now.plus(Duration.ofDays(14)), null, false));
    return new Tokens(access, refresh, 900);
  }

  private String encode(JwtClaimsSet claims) {
    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }

  public static String hash(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
