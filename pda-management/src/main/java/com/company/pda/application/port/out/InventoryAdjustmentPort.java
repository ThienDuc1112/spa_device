package com.company.pda.application.port.out;

import com.company.pda.domain.shared.model.Actor;
import java.math.BigDecimal;
import java.util.UUID;

public interface InventoryAdjustmentPort {
  void lockStore(long storeId);

  void deduct(Actor actor, long productId, BigDecimal quantity, UUID disposalId);
}
