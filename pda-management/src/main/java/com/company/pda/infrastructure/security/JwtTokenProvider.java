package com.company.pda.infrastructure.security;

import com.company.pda.application.port.out.TokenProvider;
import com.company.pda.domain.auth.model.User;
import com.company.pda.domain.shared.exception.DomainException;
import io.jsonwebtoken.*;
import java.time.Instant;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProvider {
  private final SecretKeySpec key;
  private final String issuer;
  private final JwtParser parser;

  public JwtTokenProvider(SecretKeySpec key, @Value("${app.issuer}") String issuer) {
    this.key = key;
    this.issuer = issuer;
    this.parser = Jwts.parserBuilder().setSigningKey(key).requireIssuer(issuer).build();
  }

  public String access(User user, List<String> roles, Instant now, Instant expiresAt, UUID family) {
    return builder(user, now, expiresAt)
        .setAudience("retail-api")
        .claim("sessionId", family.toString())
        .claim("type", "access")
        .claim("storeId", user.storeId())
        .claim("roles", roles)
        .compact();
  }

  public String refresh(User user, UUID id, Instant now, Instant expiresAt) {
    return builder(user, now, expiresAt)
        .setAudience("retail-refresh")
        .setId(id.toString())
        .claim("type", "refresh")
        .compact();
  }

  public Claims verifyAccess(String token) {
    try {
      return verify(token, "access", "retail-api");
    } catch (ExpiredJwtException e) {
      throw new JwtAuthenticationException("ACCESS_TOKEN_EXPIRED");
    } catch (JwtException | IllegalArgumentException e) {
      throw new JwtAuthenticationException("ACCESS_TOKEN_INVALID");
    }
  }

  public UUID verifyRefresh(String token) {
    try {
      String id = verify(token, "refresh", "retail-refresh").getId();
      if (id == null) throw new MalformedJwtException("Missing refresh token ID");
      return UUID.fromString(id);
    } catch (ExpiredJwtException e) {
      throw new DomainException(401, "SESSION_EXPIRED", "Session expired; sign in again");
    } catch (JwtException | IllegalArgumentException e) {
      throw new DomainException(401, "REFRESH_TOKEN_INVALID", "Invalid refresh token");
    }
  }

  private JwtBuilder builder(User user, Instant now, Instant expiresAt) {
    return Jwts.builder()
        .setIssuer(issuer)
        .setSubject(Long.toString(user.id()))
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(expiresAt))
        .signWith(key, SignatureAlgorithm.HS256);
  }

  private Claims verify(String token, String type, String audience) {
    Jws<Claims> jwt = parser.parseClaimsJws(token);
    Claims claims = jwt.getBody();
    // Accept both the JWT string audience and the array emitted by the previous issuer.
    Object audiences = claims.get(Claims.AUDIENCE);
    boolean matchesAudience =
        audience.equals(audiences)
            || (audiences instanceof Collection && ((Collection<?>) audiences).contains(audience));
    if (!SignatureAlgorithm.HS256.getValue().equals(jwt.getHeader().getAlgorithm())
        || !type.equals(claims.get("type"))
        || !matchesAudience
        || claims.getExpiration() == null
        || claims.getSubject() == null) throw new MalformedJwtException("Invalid token claims");
    if (!claims.getExpiration().toInstant().isAfter(Instant.now()))
      throw new ExpiredJwtException(jwt.getHeader(), claims, "Token expired");
    return claims;
  }
}
