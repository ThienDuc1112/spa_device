package com.company.pda.infrastructure.security;

import com.company.pda.application.port.out.TokenProvider;
import com.company.pda.domain.auth.model.User;
import com.company.pda.domain.shared.exception.DomainException;
import java.time.*;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProvider {
  private final JwtEncoder encoder;
  private final JwtDecoder refreshDecoder;
  private final String issuer;

  public JwtTokenProvider(
      JwtEncoder encoder, SecretKeySpec key, @Value("${app.issuer}") String issuer) {
    this.encoder = encoder;
    this.issuer = issuer;
    lombok.val decoder =
        NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    decoder.setJwtValidator(
        new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(issuer),
            jwt ->
                jwt.getExpiresAt() != null && jwt.getExpiresAt().isAfter(Instant.now())
                    ? org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success()
                    : org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                        new org.springframework.security.oauth2.core.OAuth2Error(
                            "SESSION_EXPIRED"))));
    refreshDecoder = decoder;
  }

  public String access(User user, List<String> roles, Instant now, Instant expiresAt, UUID family) {
    return encode(
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject("" + user.id())
            .audience(java.util.Arrays.asList("retail-api"))
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim("sessionId", family.toString())
            .claim("type", "access")
            .claim("storeId", user.storeId())
            .claim("roles", roles)
            .build());
  }

  public String refresh(User user, UUID id, Instant now, Instant expiresAt) {
    return encode(
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject("" + user.id())
            .audience(java.util.Arrays.asList("retail-refresh"))
            .id(id.toString())
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim("type", "refresh")
            .build());
  }

  public UUID verifyRefresh(String token) {
    try {
      lombok.val jwt = refreshDecoder.decode(token);
      if (!"refresh".equals(jwt.getClaimAsString("type"))
          || !jwt.getAudience().contains("retail-refresh"))
        throw new DomainException(401, "REFRESH_TOKEN_INVALID", "Invalid refresh token");
      return UUID.fromString(jwt.getId());
    } catch (JwtValidationException e) {
      boolean expired =
          e.getErrors().stream().anyMatch(error -> "SESSION_EXPIRED".equals(error.getErrorCode()));
      throw new DomainException(
          401,
          expired ? "SESSION_EXPIRED" : "REFRESH_TOKEN_INVALID",
          expired ? "Session expired; sign in again" : "Invalid refresh token");
    } catch (JwtException | IllegalArgumentException e) {
      throw new DomainException(401, "REFRESH_TOKEN_INVALID", "Invalid refresh token");
    }
  }

  private String encode(JwtClaimsSet claims) {
    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }
}
