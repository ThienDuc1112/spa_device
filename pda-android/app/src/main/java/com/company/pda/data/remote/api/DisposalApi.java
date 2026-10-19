package com.company.pda.data.remote.api;

import com.company.pda.common.util.ApiCalls;
import com.company.pda.data.remote.dto.disposal.DisposalDto;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class DisposalApi {
  private final ApiCalls api;
  private final Executor executor;

  public DisposalApi(ApiCalls api, Executor executor) {
    this.api = api;
    this.executor = executor;
  }

  public CompletableFuture<List<DisposalDto>> list(int page) {
    return api.callApi(
        "GET",
        "disposals?page=" + page,
        null,
        new TypeToken<List<DisposalDto>>() {}.getType(),
        Map.of(),
        executor);
  }

  public CompletableFuture<DisposalDto.Detail> detail(String id) {
    return api.callApi(
        "GET",
        "disposals/" + ApiCalls.path(id),
        null,
        DisposalDto.Detail.class,
        Map.of(),
        executor);
  }

  public CompletableFuture<DisposalDto> create(DisposalDto.Create body) {
    return api.callApi("POST", "disposals", body, DisposalDto.class, Map.of(), executor);
  }

  public CompletableFuture<DisposalDto> confirm(String id, DisposalDto.Transition body) {
    return api.callApi(
        "POST",
        "disposals/" + ApiCalls.path(id) + "/confirm",
        body,
        DisposalDto.class,
        Map.of(),
        executor);
  }

  public CompletableFuture<DisposalDto> cancel(String id, DisposalDto.Transition body) {
    return api.callApi(
        "POST",
        "disposals/" + ApiCalls.path(id) + "/cancel",
        body,
        DisposalDto.class,
        Map.of(),
        executor);
  }
}
