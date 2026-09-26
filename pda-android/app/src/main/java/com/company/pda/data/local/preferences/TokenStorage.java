package com.company.pda.data.local.preferences;

import android.content.*;
import android.security.keystore.*;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;

public final class TokenStorage {

  private final android.content.SharedPreferences prefs;

  private final javax.crypto.SecretKey key;

  public TokenStorage(Context context) {

    prefs = context.getSharedPreferences("secure", Context.MODE_PRIVATE);

    try {

      var ks = KeyStore.getInstance("AndroidKeyStore");

      ks.load(null);

      String alias = "retail-pda-v1";

      if (!ks.containsAlias(alias)) {

        var gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

        gen.init(
            new KeyGenParameterSpec.Builder(
                    alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());

        gen.generateKey();
      }

      key = (javax.crypto.SecretKey) ks.getKey(alias, null);

    } catch (Exception e) {

      throw new IllegalStateException("Secure storage unavailable", e);
    }
  }

  public synchronized String get(String name) {

    String value = prefs.getString(name, null);

    if (value == null) return null;

    try {

      String[] parts = value.split(":");

      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");

      c.init(
          Cipher.DECRYPT_MODE,
          key,
          new GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)));

      return new String(c.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), StandardCharsets.UTF_8);

    } catch (Exception e) {

      prefs.edit().remove(name).commit();

      return null;
    }
  }

  public synchronized void put(String name, String value) {

    if (value == null) {

      prefs.edit().remove(name).commit();

      return;
    }

    try {

      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");

      c.init(Cipher.ENCRYPT_MODE, key);

      String result =
          Base64.encodeToString(c.getIV(), Base64.NO_WRAP)
              + ":"
              + Base64.encodeToString(
                  c.doFinal(value.getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP);

      if (!prefs.edit().putString(name, result).commit())
        throw new IllegalStateException("Storage write failed");

    } catch (Exception e) {

      throw new IllegalStateException("Secure storage write failed", e);
    }
  }

  public synchronized void tokens(com.company.pda.data.remote.dto.auth.AuthDto.Tokens t) {

    put("session", new com.google.gson.Gson().toJson(t));
  }

  public synchronized com.company.pda.data.remote.dto.auth.AuthDto.Tokens tokens() {

    String s = get("session");

    return s == null
        ? null
        : new com.google.gson.Gson()
            .fromJson(s, com.company.pda.data.remote.dto.auth.AuthDto.Tokens.class);
  }

  public synchronized void logout() {

    put("session", null);
  }
}
