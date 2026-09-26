package com.company;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.company.application.*;
import com.company.application.dto.Contracts.*;
import com.company.domain.*;
import com.company.domain.Models.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.*;

class InventoryServiceTest {
  InventoryRepository repo = mock(InventoryRepository.class);
  ProductRepository products = mock(ProductRepository.class);
  CurrentActor actor = mock(CurrentActor.class);
  OperationsRepository ops = mock(OperationsRepository.class);
  InventoryService service = new InventoryService(repo, products, actor, ops);

  @BeforeEach
  void setup() {
    when(repo.adjustmentById(any(), anyLong())).thenReturn(null);
    when(actor.get()).thenReturn(new Actor(1, 1));
    when(products.code("P1")).thenReturn(new Product(1, "123", "P1", "Product", null, 0));
    when(repo.find(1, 1)).thenReturn(new Inventory(1, 1, 1, new BigDecimal("10.00"), 2));
  }

  @Test
  void staleVersionCannotWrite() {
    assertThrows(
        BusinessException.class,
        () -> service.adjust(new Adjustment(UUID.randomUUID(), "P1", BigDecimal.ONE, 1L, "count")));
    verify(repo, never()).update(anyLong(), anyLong(), any(), anyLong());
  }

  @Test
  void concurrentChangeCannotWriteLedger() {
    when(repo.update(1, 1, BigDecimal.ONE, 2)).thenReturn(0);
    assertThrows(
        BusinessException.class,
        () -> service.adjust(new Adjustment(UUID.randomUUID(), "P1", BigDecimal.ONE, 2L, "count")));
    verify(repo, never())
        .transaction(anyLong(), anyLong(), any(), any(), any(), any(), any(), any(), anyLong());
  }

  @Test
  void insufficientStockCannotDeduct() {
    assertThrows(
        BusinessException.class,
        () -> service.deduct(new Actor(1, 1), 1, new BigDecimal("11"), UUID.randomUUID()));
    verify(repo, never()).update(anyLong(), anyLong(), any(), anyLong());
  }

  @Test
  void negativeDisposalCannotIncreaseStock() {
    assertThrows(
        BusinessException.class,
        () -> service.deduct(new Actor(1, 1), 1, new BigDecimal("-1"), UUID.randomUUID()));
  }

  @Test
  void validAdjustmentProducesLedger() {
    when(repo.update(1, 1, BigDecimal.ONE, 2)).thenReturn(1);
    when(repo.transaction(
            anyLong(), anyLong(), any(), any(), any(), any(), any(), any(), anyLong()))
        .thenReturn(1L);
    UUID id = UUID.randomUUID();
    service.adjust(new Adjustment(id, "P1", BigDecimal.ONE, 2L, "count"));
    verify(repo)
        .transaction(
            1,
            1,
            "ADJUSTMENT",
            new BigDecimal("10.00"),
            new BigDecimal("-9.00"),
            BigDecimal.ONE,
            id,
            null,
            1);
  }
}
