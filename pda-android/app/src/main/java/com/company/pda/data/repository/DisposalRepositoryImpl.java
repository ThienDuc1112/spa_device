package com.company.pda.data.repository;

import static com.company.pda.common.util.ApiCalls.execute;

import com.company.pda.data.remote.api.DisposalApi;
import com.company.pda.data.remote.dto.disposal.DisposalDto;
import com.company.pda.domain.model.*;
import com.company.pda.domain.repository.DisposalRepository;
import java.math.BigDecimal;
import java.util.*;

public class DisposalRepositoryImpl implements DisposalRepository {
  private final DisposalApi api;

  public DisposalRepositoryImpl(DisposalApi api) {
    this.api = api;
  }

  private Disposal map(DisposalDto dto) {
    var d = new Disposal();
    d.id = dto.id;
    d.status = dto.status;
    d.remarks = dto.remarks;
    d.createdAt = dto.createdAt;
    d.version = dto.version;
    return d;
  }

  public List<Disposal> list(int page) throws java.io.IOException {
    var result = new ArrayList<Disposal>();
    for (var dto : execute(api.list(page))) result.add(map(dto));
    return result;
  }

  public DisposalDetail detail(String id) throws java.io.IOException {
    var dto = execute(api.detail(id));
    var d = new DisposalDetail();
    d.disposal = map(dto.disposal);
    d.items = new ArrayList<>();
    for (var i : dto.items) {
      var item = new DisposalItem();
      item.productId = i.productId;
      item.quantity = i.quantity;
      item.reason = i.reason;
      d.items.add(item);
    }
    d.history = dto.history;
    return d;
  }

  public Disposal create(String id, String code, BigDecimal quantity, String reason)
      throws java.io.IOException {
    return map(
        execute(
            api.create(
                new DisposalDto.Create(
                    id, reason, List.of(new DisposalDto.Line(code, quantity, reason))))));
  }

  public Disposal transition(String id, long version, boolean confirm) throws java.io.IOException {
    var body = new DisposalDto.Transition(version);
    return map(execute(confirm ? api.confirm(id, body) : api.cancel(id, body)));
  }
}
