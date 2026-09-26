package com.company.application;

import static com.company.domain.BusinessException.found;

import com.company.application.dto.Contracts.*;
import com.company.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService implements ProductUseCase {
  private final ProductRepository repo;
  private final ProductDtoMapper mapper;
  private final CurrentActor actor;
  private final OperationsRepository ops;

  public ProductService(
      ProductRepository repo,
      ProductDtoMapper mapper,
      CurrentActor actor,
      OperationsRepository ops) {
    this.repo = repo;
    this.mapper = mapper;
    this.actor = actor;
    this.ops = ops;
  }

  public ProductDto lookup(String barcode) {
    actor.get();
    return mapper.toDto(found(repo.barcode(barcode)));
  }

  @Transactional
  public void sync(ImageSync body) {
    var a = actor.get();
    var p = found(repo.code(body.productCode()));
    int changed = repo.image(p.id(), body.imageUrl(), body.sourceVersion());
    repo.imageLog(
        p.id(), body.imageUrl(), body.sourceVersion(), changed == 1 ? "APPLIED" : "STALE", a.id());
    ops.audit(a.id(), a.storeId(), "IMAGE_SYNC", body.productCode());
  }
}
