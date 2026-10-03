package com.company.pda.application.disposal.service;

import static com.company.pda.domain.shared.exception.DomainException.*;

import com.company.pda.application.disposal.dto.CreateDisposalCommand;
import com.company.pda.application.disposal.usecase.DisposalUseCase;
import com.company.pda.application.port.out.AuditLogPort;
import com.company.pda.application.port.out.CurrentActor;
import com.company.pda.application.port.out.InventoryAdjustmentPort;
import com.company.pda.domain.disposal.model.Disposal;
import com.company.pda.domain.disposal.model.DisposalItem;
import com.company.pda.domain.disposal.model.DisposalStatus;
import com.company.pda.domain.disposal.repository.DisposalRepository;
import com.company.pda.domain.product.repository.ProductRepository;
import com.company.pda.domain.shared.exception.DomainException;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisposalService implements DisposalUseCase {
  private final DisposalRepository repo;
  private final ProductRepository products;
  private final InventoryAdjustmentPort inventory;
  private final CurrentActor actor;
  private final AuditLogPort ops;

  public DisposalService(
      DisposalRepository repo,
      ProductRepository products,
      InventoryAdjustmentPort inventory,
      CurrentActor actor,
      AuditLogPort ops) {
    this.repo = repo;
    this.products = products;
    this.inventory = inventory;
    this.actor = actor;
    this.ops = ops;
  }

  public java.util.List<Disposal> list(int page) {
    if (page < 0 || page > 10000) throw new DomainException(400, "Invalid page");
    return repo.list(actor.get().storeId(), page * 50);
  }

  @Transactional
  public Disposal confirm(UUID id, long version) {
    return transition(id, version, true);
  }

  @Transactional
  public Disposal cancel(UUID id, long version) {
    return transition(id, version, false);
  }

  @Transactional(readOnly = true)
  public Object detail(UUID id) {
    lombok.val d = found(repo.find(id, actor.get().storeId()));
    Map<String, Object> detail = new LinkedHashMap<>();
    detail.put("disposal", d);
    detail.put("items", repo.items(id));
    detail.put("history", repo.history(id));
    return Collections.unmodifiableMap(detail);
  }

  @Transactional
  public Disposal create(CreateDisposalCommand body) {
    lombok.val a = actor.get();
    lombok.val old = repo.find(body.requestId(), a.storeId());
    lombok.val items =
        body.items().stream()
            .map(
                i ->
                    new DisposalItem(
                        found(products.code(i.productCode())).id(), i.quantity(), i.reason()))
            .sorted(Comparator.comparingLong(DisposalItem::productId))
            .collect(java.util.stream.Collectors.toList());
    require(
        items.stream().map(DisposalItem::productId).distinct().count() == items.size(),
        "Duplicate product in disposal");
    if (old != null) {
      lombok.val stored = repo.items(old.id());
      boolean same = stored.size() == items.size();
      for (int i = 0; same && i < items.size(); i++) {
        lombok.val x = items.get(i);
        lombok.val y = stored.get(i);
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
    for (lombok.val i : items) repo.item(body.requestId(), i.productId(), i.quantity(), i.reason());
    repo.historyAdd(body.requestId(), null, DisposalStatus.PENDING.name(), a.id());
    ops.audit(a.id(), a.storeId(), "DISPOSAL_CREATE", body.requestId().toString());
    return found(repo.find(body.requestId(), a.storeId()));
  }

  @Transactional
  public Disposal transition(UUID id, long version, boolean confirm) {
    lombok.val a = actor.get();
    inventory.lockStore(a.storeId());
    lombok.val d = found(repo.lock(id, a.storeId()));
    String target = confirm ? DisposalStatus.CONFIRMED.name() : DisposalStatus.CANCELLED.name();
    if (d.status().equals(target)) return d;
    require(d.status().equals(DisposalStatus.PENDING.name()), "Disposal is already finalized");
    require(d.version() == version, "Disposal changed; reload");
    if (confirm)
      for (lombok.val i : repo.items(id)) inventory.deduct(a, i.productId(), i.quantity(), id);
    require(repo.transition(id, target, version) == 1, "Disposal changed; reload");
    repo.historyAdd(id, d.status(), target, a.id());
    ops.audit(a.id(), a.storeId(), "DISPOSAL_" + target, id.toString());
    return found(repo.find(id, a.storeId()));
  }
}
