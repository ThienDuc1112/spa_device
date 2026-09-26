package com.company.presentation;

import com.company.application.ProductUseCase;
import com.company.application.dto.Contracts.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {
  private final ProductUseCase service;

  public ProductController(ProductUseCase service) {
    this.service = service;
  }

  @GetMapping("/products/barcode/{barcode}")
  public ProductDto lookup(@PathVariable String barcode) {
    return service.lookup(barcode);
  }

  @PutMapping("/products/image-sync")
  @PreAuthorize("hasRole('ERP')")
  public void sync(@Valid @RequestBody ImageSync body) {
    service.sync(body);
  }
}
