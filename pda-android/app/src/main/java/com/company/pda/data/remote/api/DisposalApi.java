package com.company.pda.data.remote.api;

import com.company.pda.common.util.ApiCalls;
import com.company.pda.data.remote.dto.disposal.DisposalDto;
import com.google.gson.reflect.TypeToken;
import java.util.List;

public final class DisposalApi {
  private final ApiCalls api;

  public DisposalApi(ApiCalls api) {
    this.api = api;
  }

  public ApiCalls.Request<List<DisposalDto>> list(int page) {
    return api.request(
        "GET", "disposals?page=" + page, null, new TypeToken<List<DisposalDto>>() {}.getType());
  }

  public ApiCalls.Request<DisposalDto.Detail> detail(String id) {
    return api.request("GET", "disposals/" + ApiCalls.path(id), null, DisposalDto.Detail.class);
  }

  public ApiCalls.Request<DisposalDto> create(DisposalDto.Create body) {
    return api.request("POST", "disposals", body, DisposalDto.class);
  }

  public ApiCalls.Request<DisposalDto> confirm(String id, DisposalDto.Transition body) {
    return api.request(
        "POST", "disposals/" + ApiCalls.path(id) + "/confirm", body, DisposalDto.class);
  }

  public ApiCalls.Request<DisposalDto> cancel(String id, DisposalDto.Transition body) {
    return api.request(
        "POST", "disposals/" + ApiCalls.path(id) + "/cancel", body, DisposalDto.class);
  }
}
