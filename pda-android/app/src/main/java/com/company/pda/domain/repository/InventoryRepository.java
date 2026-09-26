package com.company.pda.domain.repository;

import com.company.pda.domain.model.*;

public interface InventoryRepository {
  Inventory get(String code) throws java.io.IOException;

  Inventory adjust(
      String requestId, String code, java.math.BigDecimal quantity, long version, String reason)
      throws java.io.IOException;
}
