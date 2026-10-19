package com.company.pda.data.remote.api;

import com.company.pda.common.util.ApiCalls;
import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto.*;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class PdaFinderApi {
  private final ApiCalls api;
  private final Executor executor;

  public PdaFinderApi(ApiCalls api, Executor executor) {
    this.api = api;
    this.executor = executor;
  }

  public CompletableFuture<FcmHealth> fcmHealth(long id, String secret) {
    return api.callApi(
        "GET",
        "pda/fcm-health",
        null,
        FcmHealth.class,
        Map.of("X-Device-Id", Long.toString(id), "X-Device-Secret", secret),
        executor);
  }

  public CompletableFuture<List<Command>> commands(long id, String secret) {
    return api.callApi(
        "GET",
        "pda/commands",
        null,
        new TypeToken<List<Command>>() {}.getType(),
        Map.of("X-Device-Id", Long.toString(id), "X-Device-Secret", secret),
        executor);
  }

  public CompletableFuture<Registration> register(Register body) {
    return api.callApi("POST", "devices/register", body, Registration.class, Map.of(), executor);
  }

  public CompletableFuture<Void> token(long id, String secret, Token body) {
    return api.callApi(
        "PUT",
        "devices/token",
        body,
        Void.class,
        Map.of("X-Device-Id", Long.toString(id), "X-Device-Secret", secret),
        executor);
  }

  public CompletableFuture<Void> event(long id, String secret, Event body) {
    return api.callApi(
        "POST",
        "pda/events",
        body,
        Void.class,
        Map.of("X-Device-Id", Long.toString(id), "X-Device-Secret", secret),
        executor);
  }
}
