package com.company.pda.application.inventory.service;

import static com.company.pda.domain.shared.exception.DomainException.*;

import com.company.pda.application.inventory.dto.AdjustInventoryCommand;
import com.company.pda.application.inventory.usecase.InventoryUseCase;
import com.company.pda.application.port.out.AuditLogPort;
import com.company.pda.application.port.out.CurrentActor;
import com.company.pda.application.port.out.InventoryAdjustmentPort;
import com.company.pda.domain.inventory.exception.InsufficientInventoryException;
import com.company.pda.domain.inventory.model.Inventory;
import com.company.pda.domain.inventory.repository.InventoryRepository;
import com.company.pda.domain.product.repository.ProductRepository;
import com.company.pda.domain.shared.model.Actor;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService implements InventoryUseCase, InventoryAdjustmentPort {
  private final InventoryRepository repo;
  private final ProductRepository products;
  private final CurrentActor actor;
  private final AuditLogPort ops;

  public InventoryService(
      InventoryRepository repo, ProductRepository products, CurrentActor actor, AuditLogPort ops) {
    this.repo = repo;
    this.products = products;
    this.actor = actor;
    this.ops = ops;
  }

  public Inventory get(String code) {
    return found(repo.find(actor.get().storeId(), found(products.code(code)).id()));
  }

  public java.util.List<java.util.Map<String, Object>> history(long afterId) {
    return repo.transactions(actor.get().storeId(), afterId);
  }

  @Override
  public void lockStore(long storeId) {
    repo.lockStore(storeId);
  }

  @Transactional
  public Inventory adjust(AdjustInventoryCommand b) {
    var a = actor.get();
    repo.lockStore(a.storeId());
    var p = found(products.code(b.productCode()));
    var previous = repo.adjustmentById(b.requestId(), a.storeId());
    if (previous != null) {
      require(
          previous.productId() == p.id()
              && previous.newQty().compareTo(b.quantity()) == 0
              && previous.reason().equals(b.reason())
              && previous.expectedVersion() == b.version()
              && previous.createdBy() == a.id(),
          "Request ID already used with different content");
      return found(repo.find(a.storeId(), p.id()));
    }
    var stock = found(repo.find(a.storeId(), p.id()));
    require(stock.version() == b.version(), "Inventory changed; reload and retry");
    require(
        repo.update(a.storeId(), p.id(), b.quantity(), b.version()) == 1,
        "Inventory changed; reload and retry");
    repo.adjustment(
        b.requestId(),
        a.storeId(),
        p.id(),
        stock.quantity(),
        b.quantity(),
        b.version(),
        b.reason(),
        a.id());
    transaction(a, p.id(), stock.quantity(), b.quantity(), b.requestId(), null);
    ops.audit(a.id(), a.storeId(), "INVENTORY_ADJUST", b.requestId().toString());
    return found(repo.find(a.storeId(), p.id()));
  }

  @Override
  public void deduct(Actor a, long productId, BigDecimal quantity, UUID disposalId) {
    require(quantity.signum() > 0, "Quantity must be positive");
    var stock = found(repo.find(a.storeId(), productId));
    var next = stock.quantity().subtract(quantity);
    if (next.signum() < 0) throw new InsufficientInventoryException();
    require(
        repo.update(a.storeId(), productId, next, stock.version()) == 1,
        "Inventory changed; retry confirmation");
    transaction(a, productId, stock.quantity(), next, null, disposalId);
  }

  private void transaction(
      Actor a,
      long productId,
      BigDecimal before,
      BigDecimal after,
      UUID adjustment,
      UUID disposal) {
    long id =
        repo.transaction(
            a.storeId(),
            productId,
            adjustment == null ? "DISPOSAL" : "ADJUSTMENT",
            before,
            after.subtract(before),
            after,
            adjustment,
            disposal,
            a.id());
    // ERP pulls the immutable ledger with a monotonic cursor, avoiding a second write or missed
    // updates.
    ops.audit(a.id(), a.storeId(), "INVENTORY_TRANSACTION", Long.toString(id));
  }
}
