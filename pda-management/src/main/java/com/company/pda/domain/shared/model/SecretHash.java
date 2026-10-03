package com.company.pda.domain.shared.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class SecretHash {
  private SecretHash() {}

  public static String hash(String value) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(digest.length * 2);
      for (byte b : digest) {
        hex.append(Character.forDigit((b & 0xff) >>> 4, 16));
        hex.append(Character.forDigit(b & 0x0f, 16));
      }
      return hex.toString();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
