package com.company.pda.data.repository;

import static com.company.pda.common.util.ApiCalls.execute;

import com.company.pda.data.mapper.InventoryMapper;
import com.company.pda.data.remote.api.InventoryApi;
import com.company.pda.data.remote.dto.inventory.InventoryDto;
import com.company.pda.domain.model.Inventory;
import com.company.pda.domain.repository.InventoryRepository;
import java.math.BigDecimal;

public class InventoryRepositoryImpl implements InventoryRepository {
  private final InventoryApi api;
  private final InventoryMapper mapper = new InventoryMapper();

  public InventoryRepositoryImpl(InventoryApi api) {
    this.api = api;
  }

  public Inventory get(String code) throws java.io.IOException {
    return mapper.toDomain(execute(api.inventory(code)));
  }

  public Inventory adjust(String id, String code, BigDecimal quantity, long version, String reason)
      throws java.io.IOException {
    return mapper.toDomain(
        execute(api.adjust(new InventoryDto.Adjustment(id, code, quantity, version, reason))));
  }
}
