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
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
    refreshDecoder = decoder;
  }

  public String access(User user, List<String> roles, Instant now) {
    return encode(
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject("" + user.id())
            .audience(java.util.Arrays.asList("retail-api"))
            .issuedAt(now)
            .expiresAt(now.plusSeconds(900))
            .claim("type", "access")
            .claim("storeId", user.storeId())
            .claim("roles", roles)
            .build());
  }

  public String refresh(User user, UUID id, Instant now) {
    return encode(
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject("" + user.id())
            .audience(java.util.Arrays.asList("retail-refresh"))
            .id(id.toString())
            .issuedAt(now)
            .expiresAt(now.plus(Duration.ofDays(14)))
            .claim("type", "refresh")
            .build());
  }

  public UUID verifyRefresh(String token) {
    try {
      lombok.val jwt = refreshDecoder.decode(token);
      if (!"refresh".equals(jwt.getClaimAsString("type"))
          || !jwt.getAudience().contains("retail-refresh"))
        throw new DomainException(401, "Invalid refresh token");
      return UUID.fromString(jwt.getId());
    } catch (JwtException | IllegalArgumentException e) {
      throw new DomainException(401, "Invalid refresh token");
    }
  }

  private String encode(JwtClaimsSet claims) {
    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }
}
