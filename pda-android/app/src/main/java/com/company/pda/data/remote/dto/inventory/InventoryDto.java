package com.company.pda.data.remote.dto.inventory;

public class InventoryDto {
  public long id, productId, storeId, version;
  public java.math.BigDecimal quantity;

  public record Adjustment(
      String requestId,
      String productCode,
      java.math.BigDecimal quantity,
      long version,
      String reason) {}
}
