package com.company.pda.presentation.rest.inventory;

import com.company.pda.application.inventory.usecase.InventoryUseCase;
import com.company.pda.presentation.rest.inventory.dto.AdjustInventoryRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class InventoryController {
  private final InventoryUseCase service;

  public InventoryController(InventoryUseCase service) {
    this.service = service;
  }

  @GetMapping("/inventories/{productCode}")
  public Object get(@PathVariable String productCode) {
    return service.get(productCode);
  }

  @PostMapping("/inventory-adjustments")
  @PreAuthorize("hasRole('MANAGER')")
  public Object adjust(@Valid @RequestBody AdjustInventoryRequest body) {
    return service.adjust(body.toCommand());
  }

  @GetMapping("/inventory-transactions")
  @PreAuthorize("hasAnyRole('MANAGER','ERP')")
  public Object history(@RequestParam(defaultValue = "0") long afterId) {
    return service.history(afterId);
  }
}
