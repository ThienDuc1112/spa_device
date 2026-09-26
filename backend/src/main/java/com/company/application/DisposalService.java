package com.company.application;

import static com.company.domain.BusinessException.*;

import com.company.application.dto.Contracts.*;
import com.company.domain.*;
import com.company.domain.Models.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisposalService implements DisposalUseCase {
  private final DisposalRepository repo;
  private final ProductRepository products;
  private final InventoryService inventory;
  private final CurrentActor actor;
  private final OperationsRepository ops;

  public DisposalService(
      DisposalRepository repo,
      ProductRepository products,
      InventoryService inventory,
      CurrentActor actor,
      OperationsRepository ops) {
    this.repo = repo;
    this.products = products;
    this.inventory = inventory;
    this.actor = actor;
    this.ops = ops;
  }

  @Transactional(readOnly = true)
  public Object detail(UUID id) {
    var d = found(repo.find(id, actor.get().storeId()));
    return Map.of("disposal", d, "items", repo.items(id), "history", repo.history(id));
  }

  @Transactional
  public Disposal create(DisposalCreate body) {
    var a = actor.get();
    var old = repo.find(body.requestId(), a.storeId());
    var items =
        body.items().stream()
            .map(
                i ->
                    new DisposalItem(
                        found(products.code(i.productCode())).id(), i.quantity(), i.reason()))
            .sorted(Comparator.comparingLong(DisposalItem::productId))
            .toList();
    require(
        items.stream().map(DisposalItem::productId).distinct().count() == items.size(),
        "Duplicate product in disposal");
    if (old != null) {
      var stored = repo.items(old.id());
      boolean same = stored.size() == items.size();
      for (int i = 0; same && i < items.size(); i++) {
        var x = items.get(i);
        var y = stored.get(i);
        same =
            x.productId() == y.productId()
                && x.quantity().compareTo(y.quantity()) == 0
                && x.reason().equals(y.reason());
      }
      require(
          same && old.remarks().equals(body.remarks()) && old.createdBy() == a.id(),
          "Request ID already used with different content");
      return old;
    }
    repo.create(body.requestId(), a.storeId(), body.remarks(), a.id());
    for (var i : items) repo.item(body.requestId(), i.productId(), i.quantity(), i.reason());
    repo.historyAdd(body.requestId(), null, "PENDING", a.id());
    ops.audit(a.id(), a.storeId(), "DISPOSAL_CREATE", body.requestId().toString());
    return found(repo.find(body.requestId(), a.storeId()));
  }

  @Transactional
  public Disposal transition(UUID id, long version, boolean confirm) {
    var a = actor.get();
    inventory.lockStore(a.storeId());
    var d = found(repo.lock(id, a.storeId()));
    String target = confirm ? "CONFIRMED" : "CANCELLED";
    if (d.status().equals(target)) return d;
    require(d.status().equals("PENDING"), "Disposal is already finalized");
    require(d.version() == version, "Disposal changed; reload");
    if (confirm) for (var i : repo.items(id)) inventory.deduct(a, i.productId(), i.quantity(), id);
    require(repo.transition(id, target, version) == 1, "Disposal changed; reload");
    repo.historyAdd(id, d.status(), target, a.id());
    ops.audit(a.id(), a.storeId(), "DISPOSAL_" + target, id.toString());
    return found(repo.find(id, a.storeId()));
  }
}
