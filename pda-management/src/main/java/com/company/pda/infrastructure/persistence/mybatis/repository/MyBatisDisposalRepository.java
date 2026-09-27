package com.company.pda.infrastructure.persistence.mybatis.repository;

import com.company.pda.domain.disposal.model.Disposal;
import com.company.pda.domain.disposal.model.DisposalItem;
import com.company.pda.domain.disposal.repository.DisposalRepository;
import com.company.pda.infrastructure.persistence.mybatis.converter.DisposalEntityMapper;
import com.company.pda.infrastructure.persistence.mybatis.converter.DisposalItemEntityMapper;
import com.company.pda.infrastructure.persistence.mybatis.mapper.DisposalMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisDisposalRepository implements DisposalRepository {
  private final DisposalMapper mapper;

  public MyBatisDisposalRepository(DisposalMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Disposal find(UUID id, long storeId) {
    return DisposalEntityMapper.toDomain(mapper.find(id, storeId));
  }

  @Override
  public Disposal lock(UUID id, long storeId) {
    return DisposalEntityMapper.toDomain(mapper.lock(id, storeId));
  }

  @Override
  public java.util.List<Disposal> list(long storeId, int offset) {
    return mapper.list(storeId, offset).stream().map(DisposalEntityMapper::toDomain).toList();
  }

  @Override
  public java.util.List<DisposalItem> items(UUID id) {
    return mapper.items(id).stream().map(DisposalItemEntityMapper::toDomain).toList();
  }

  @Override
  public java.util.List<java.util.Map<String, Object>> history(UUID id) {
    return mapper.history(id);
  }

  @Override
  public int create(UUID id, long storeId, String remarks, long actorId) {
    return mapper.create(id, storeId, remarks, actorId);
  }

  @Override
  public int item(UUID id, long productId, BigDecimal quantity, String reason) {
    return mapper.item(id, productId, quantity, reason);
  }

  @Override
  public int transition(UUID id, String status, long version) {
    return mapper.transition(id, status, version);
  }

  @Override
  public int historyAdd(UUID id, String oldStatus, String newStatus, long actorId) {
    return mapper.historyAdd(id, oldStatus, newStatus, actorId);
  }
}
