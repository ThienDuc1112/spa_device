package com.company.pda.data.remote.api;

import com.company.pda.common.util.ApiCalls;
import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto.*;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.Map;

public final class PdaFinderApi {
  private final ApiCalls api;

  public PdaFinderApi(ApiCalls api) {
    this.api = api;
  }

  public ApiCalls.Request<FcmHealth> fcmHealth(long id, String secret) {
    return api.request(
        "GET",
        "pda/fcm-health",
        null,
        FcmHealth.class,
        Map.of("X-Device-Id", Long.toString(id), "X-Device-Secret", secret));
  }

  public ApiCalls.Request<List<Command>> commands(long id, String secret) {
    return api.request(
        "GET",
        "pda/commands",
        null,
        new TypeToken<List<Command>>() {}.getType(),
        Map.of("X-Device-Id", Long.toString(id), "X-Device-Secret", secret));
  }

  public ApiCalls.Request<Registration> register(Register body) {
    return api.request("POST", "devices/register", body, Registration.class);
  }

  public ApiCalls.Request<Void> token(long id, String secret, Token body) {
    return api.request(
        "PUT",
        "devices/token",
        body,
        Void.class,
        Map.of("X-Device-Id", Long.toString(id), "X-Device-Secret", secret));
  }

  public ApiCalls.Request<Void> event(long id, String secret, Event body) {
    return api.request(
        "POST",
        "pda/events",
        body,
        Void.class,
        Map.of("X-Device-Id", Long.toString(id), "X-Device-Secret", secret));
  }
}
