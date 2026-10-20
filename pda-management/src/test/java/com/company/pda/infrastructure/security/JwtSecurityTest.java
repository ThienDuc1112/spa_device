package com.company.pda.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.company.pda.domain.auth.model.User;
import com.company.pda.domain.auth.repository.AuthRepository;
import com.company.pda.domain.shared.exception.DomainException;
import io.jsonwebtoken.*;
import java.time.Instant;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringJUnitWebConfig(JwtSecurityTest.Config.class)
class JwtSecurityTest {
  static final SecretKeySpec KEY = new SecretKeySpec(new byte[32], "HmacSHA256");
  static final User USER = new User(7, "user", "hash", 3, true);
  static final UUID FAMILY = UUID.randomUUID();
  @Autowired WebApplicationContext context;
  @Autowired JwtTokenProvider tokens;
  @Autowired AuthRepository sessions;
  MockMvc mvc;

  @Configuration
  @EnableWebSecurity
  @EnableWebMvc
  @Import(SecurityConfig.class)
  static class Config {
    @Bean
    static org.springframework.context.support.PropertySourcesPlaceholderConfigurer properties() {
      Properties properties = new Properties();
      properties.setProperty("app.jwt-secret", Base64.getEncoder().encodeToString(new byte[32]));
      org.springframework.context.support.PropertySourcesPlaceholderConfigurer config =
          new org.springframework.context.support.PropertySourcesPlaceholderConfigurer();
      config.setProperties(properties);
      return config;
    }

    @Bean
    JwtTokenProvider tokens(SecretKeySpec key) {
      return new JwtTokenProvider(key, "test-issuer");
    }

    @Bean
    AuthRepository sessions() {
      return mock(AuthRepository.class);
    }

    @Bean
    TestController controller() {
      return new TestController();
    }
  }

  @RestController
  static class TestController {
    @GetMapping({"/protected", "/auth/login"})
    public String protectedEndpoint() {
      return "ok";
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/manager")
    public String manager() {
      return "manager";
    }
  }

  @BeforeEach
  void setup() {
    reset(sessions);
    when(sessions.activeSession(FAMILY, USER.id())).thenReturn(true);
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void validAccessMapsRolesAndKeepsContextPerRequest() throws Exception {
    String access =
        tokens.access(
            USER, Arrays.asList("MANAGER"), Instant.now(), Instant.now().plusSeconds(60), FAMILY);
    mvc.perform(get("/manager").header("Authorization", "Bearer " + access))
        .andExpect(status().isOk());
    verify(sessions).activeSession(FAMILY, USER.id());
    Claims claims = tokens.verifyAccess(access);
    assertEquals(USER.id(), Long.parseLong(claims.getSubject()));
    assertEquals(USER.storeId(), ((Number) claims.get("storeId")).longValue());
    mvc.perform(get("/protected"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"));
    String employee =
        tokens.access(
            USER, Arrays.asList("EMPLOYEE"), Instant.now(), Instant.now().plusSeconds(60), FAMILY);
    mvc.perform(get("/manager").header("Authorization", "Bearer " + employee))
        .andExpect(status().isForbidden());
  }

  @Test
  void revokedFamilyAndExpiredAccessHaveStableErrorCodes() throws Exception {
    String access =
        tokens.access(
            USER, Collections.emptyList(), Instant.now(), Instant.now().plusSeconds(60), FAMILY);
    when(sessions.activeSession(FAMILY, USER.id())).thenReturn(false);
    assertRejected(access, "SESSION_REVOKED");
    String expired =
        tokens.access(
            USER,
            Collections.emptyList(),
            Instant.now().minusSeconds(120),
            Instant.now().minusSeconds(60),
            FAMILY);
    assertRejected(expired, "ACCESS_TOKEN_EXPIRED");
  }

  @Test
  void refreshTokensAreNotAccessTokensAndAccessTokensAreNotRefreshTokens() throws Exception {
    UUID id = UUID.randomUUID();
    String refresh = tokens.refresh(USER, id, Instant.now(), Instant.now().plusSeconds(60));
    assertEquals(id, tokens.verifyRefresh(refresh));
    assertRejected(refresh, "ACCESS_TOKEN_INVALID");
    String access =
        tokens.access(
            USER, Collections.emptyList(), Instant.now(), Instant.now().plusSeconds(60), FAMILY);
    assertEquals(
        "REFRESH_TOKEN_INVALID",
        assertThrows(DomainException.class, () -> tokens.verifyRefresh(access)).code);
    String expired =
        tokens.refresh(USER, id, Instant.now().minusSeconds(120), Instant.now().minusSeconds(60));
    assertEquals(
        "SESSION_EXPIRED",
        assertThrows(DomainException.class, () -> tokens.verifyRefresh(expired)).code);
    String missingId = signed().claim("type", "refresh").setAudience("retail-refresh").compact();
    assertEquals(
        "REFRESH_TOKEN_INVALID",
        assertThrows(DomainException.class, () -> tokens.verifyRefresh(missingId)).code);
  }

  @Test
  void rejectsInvalidSignatureIssuerAudienceAlgorithmAndMissingExpiry() throws Exception {
    byte[] otherKey = new byte[32];
    otherKey[0] = 1;
    assertRejected(
        signed()
            .signWith(new SecretKeySpec(otherKey, "HmacSHA256"), SignatureAlgorithm.HS256)
            .compact(),
        "ACCESS_TOKEN_INVALID");
    assertRejected(signed().setIssuer("another-issuer").compact(), "ACCESS_TOKEN_INVALID");
    assertRejected(signed().setAudience("another-api").compact(), "ACCESS_TOKEN_INVALID");
    assertRejected(signed().setExpiration(null).compact(), "ACCESS_TOKEN_INVALID");
    assertRejected(signed().setSubject(null).compact(), "ACCESS_TOKEN_INVALID");
    assertRejected(signed().claim("roles", Arrays.asList(42)).compact(), "ACCESS_TOKEN_INVALID");
    assertRejected(
        signed()
            .signWith(new SecretKeySpec(new byte[64], "HmacSHA512"), SignatureAlgorithm.HS512)
            .compact(),
        "ACCESS_TOKEN_INVALID");
    assertRejected("forged.token.value", "ACCESS_TOKEN_INVALID");
  }

  @Test
  void acceptsLegacyArrayAudienceWithoutSessionId() throws Exception {
    String token = signed().claim("aud", Arrays.asList("retail-api")).compact();
    mvc.perform(get("/protected").header("Authorization", "bEaReR " + token))
        .andExpect(status().isOk());
    verifyNoInteractions(sessions);
  }

  @Test
  void malformedBearerIsRejectedEvenOnPublicEndpoint() throws Exception {
    mvc.perform(get("/auth/login")).andExpect(status().isOk());
    mvc.perform(get("/auth/login").header("Authorization", "Bearer broken token"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("ACCESS_TOKEN_INVALID"));
  }

  private JwtBuilder signed() {
    return Jwts.builder()
        .setIssuer("test-issuer")
        .setSubject("7")
        .setAudience("retail-api")
        .setExpiration(Date.from(Instant.now().plusSeconds(60)))
        .claim("type", "access")
        .signWith(KEY, SignatureAlgorithm.HS256);
  }

  private void assertRejected(String token, String code) throws Exception {
    mvc.perform(get("/protected").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string("WWW-Authenticate", "Bearer"))
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.code").value(code));
  }
}
