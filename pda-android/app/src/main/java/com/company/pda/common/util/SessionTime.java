package com.company.pda.common.util;

import com.company.pda.data.remote.dto.auth.AuthDto.Tokens;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Expiry hints for UI/refresh scheduling only; authentication is verified by the server. */
public final class SessionTime {
  private SessionTime() {}

  public static long now(Tokens tokens, long localNow) {
    return tokens.serverTime > 0 && tokens.receivedAt > 0
        ? tokens.serverTime + (localNow - tokens.receivedAt)
        : localNow;
  }

  public static long sessionExpiry(Tokens tokens) {
    return tokens.sessionExpiresAt > 0 ? tokens.sessionExpiresAt : jwtExpiry(tokens.refreshToken);
  }

  public static long accessExpiry(Tokens tokens) {
    return tokens.accessExpiresAt > 0 ? tokens.accessExpiresAt : jwtExpiry(tokens.accessToken);
  }

  private static long jwtExpiry(String token) {
    try {
      String json =
          new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
      return JsonParser.parseString(json).getAsJsonObject().get("exp").getAsLong() * 1000;
    } catch (RuntimeException error) {
      return 0; // Older servers may not expose an expiry; their HTTP response remains
                // authoritative.
    }
  }
}
