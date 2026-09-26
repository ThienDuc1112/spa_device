package com.company.presentation;

import com.company.application.*;
import com.company.application.dto.Contracts.*;
import com.company.domain.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class InventoryController {
  private final InventoryUseCase service;
  private final InventoryRepository repo;
  private final CurrentActor actor;

  public InventoryController(
      InventoryUseCase service, InventoryRepository repo, CurrentActor actor) {
    this.service = service;
    this.repo = repo;
    this.actor = actor;
  }

  @GetMapping("/inventories/{productCode}")
  public Object get(@PathVariable String productCode) {
    return service.get(productCode);
  }

  @PostMapping("/inventory-adjustments")
  @PreAuthorize("hasRole('MANAGER')")
  public Object adjust(@Valid @RequestBody Adjustment body) {
    return service.adjust(body);
  }

  @GetMapping("/inventory-transactions")
  @PreAuthorize("hasAnyRole('MANAGER','ERP')")
  public Object history(@RequestParam(defaultValue = "0") long afterId) {
    return repo.transactions(actor.get().storeId(), afterId);
  }
}
