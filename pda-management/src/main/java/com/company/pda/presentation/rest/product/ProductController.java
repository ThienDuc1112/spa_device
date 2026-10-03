package com.company.pda.presentation.rest.product;

import com.company.pda.application.product.dto.ProductResult;
import com.company.pda.application.product.usecase.ProductUseCase;
import com.company.pda.presentation.rest.product.dto.ImageSyncRequest;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {
  private final ProductUseCase service;

  public ProductController(ProductUseCase service) {
    this.service = service;
  }

  @GetMapping("/products/barcode/{barcode}")
  public ProductResult lookup(@PathVariable String barcode) {
    return service.lookup(barcode);
  }

  @PutMapping("/products/image-sync")
  @PreAuthorize("hasRole('ERP')")
  public void sync(@Valid @RequestBody ImageSyncRequest body) {
    service.sync(body.toCommand());
  }
}
