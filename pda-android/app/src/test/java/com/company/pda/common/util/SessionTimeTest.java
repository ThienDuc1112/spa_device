package com.company.pda.common.util;

import static org.junit.Assert.*;

import com.company.pda.data.remote.dto.auth.AuthDto.Tokens;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.Test;

public class SessionTimeTest {
  @Test
  public void serverClockOffsetIsPreservedAcrossElapsedTime() {
    Tokens tokens = new Tokens();
    tokens.serverTime = 1000;
    tokens.receivedAt = 100000;
    assertEquals(6000, SessionTime.now(tokens, 105000));
  }

  @Test
  public void oldServerUsesSignedJwtExpiryAsSchedulingHint() {
    Tokens tokens = new Tokens();
    tokens.refreshToken =
        "header."
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("{\"exp\":7200}".getBytes(StandardCharsets.UTF_8))
            + ".signature";
    assertEquals(7200000, SessionTime.sessionExpiry(tokens));
    tokens.refreshToken = "invalid";
    assertEquals(0, SessionTime.sessionExpiry(tokens));
  }
}
