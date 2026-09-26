package com.company.pda.data.remote.dto.disposal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DisposalDto {
  public String id, status, remarks, createdAt;
  public long version;

  public static class Item {
    public long productId;
    public BigDecimal quantity;
    public String reason;
  }

  public static class Detail {
    public DisposalDto disposal;
    public List<Item> items;
    public List<Map<String, Object>> history;
  }

  public record Line(String productCode, BigDecimal quantity, String reason) {}

  public record Create(String requestId, String remarks, List<Line> items) {}

  public record Transition(long version) {}
}
