package com.company.pda.application.product.service;

import static com.company.pda.domain.shared.exception.DomainException.found;

import com.company.pda.application.port.out.AuditLogPort;
import com.company.pda.application.port.out.CurrentActor;
import com.company.pda.application.product.dto.ImageSyncCommand;
import com.company.pda.application.product.dto.ProductDtoMapper;
import com.company.pda.application.product.dto.ProductResult;
import com.company.pda.application.product.usecase.ProductUseCase;
import com.company.pda.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService implements ProductUseCase {
  private final ProductRepository repo;
  private final ProductDtoMapper mapper;
  private final CurrentActor actor;
  private final AuditLogPort ops;

  public ProductService(
      ProductRepository repo, ProductDtoMapper mapper, CurrentActor actor, AuditLogPort ops) {
    this.repo = repo;
    this.mapper = mapper;
    this.actor = actor;
    this.ops = ops;
  }

  public ProductResult lookup(String barcode) {
    actor.get();
    return mapper.toDto(found(repo.barcode(barcode)));
  }

  @Transactional
  public void sync(ImageSyncCommand body) {
    lombok.val a = actor.get();
    lombok.val p = found(repo.code(body.productCode()));
    int changed = repo.image(p.id(), body.imageUrl(), body.sourceVersion());
    repo.imageLog(
        p.id(), body.imageUrl(), body.sourceVersion(), changed == 1 ? "APPLIED" : "STALE", a.id());
    ops.audit(a.id(), a.storeId(), "IMAGE_SYNC", body.productCode());
  }
}
