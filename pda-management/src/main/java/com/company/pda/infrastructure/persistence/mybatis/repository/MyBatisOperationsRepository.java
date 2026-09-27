package com.company.pda.infrastructure.persistence.mybatis.repository;

import com.company.pda.application.port.out.OperationsRepository;
import com.company.pda.domain.shared.model.Outbox;
import com.company.pda.infrastructure.persistence.mybatis.converter.OutboxEntityMapper;
import com.company.pda.infrastructure.persistence.mybatis.mapper.OperationsMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisOperationsRepository implements OperationsRepository {
  private final OperationsMapper mapper;

  public MyBatisOperationsRepository(OperationsMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public int systemAudit(long storeId, String operation, String reference) {
    return mapper.systemAudit(storeId, operation, reference);
  }

  @Override
  public int audit(long actorId, long storeId, String operation, String reference) {
    return mapper.audit(actorId, storeId, operation, reference);
  }

  @Override
  public int enqueue(String type, String aggregateId, String payload) {
    return mapper.enqueue(type, aggregateId, payload);
  }

  @Override
  public Outbox next() {
    return OutboxEntityMapper.toDomain(mapper.next());
  }

  @Override
  public int done(long id) {
    return mapper.done(id);
  }

  @Override
  public int retry(long id) {
    return mapper.retry(id);
  }
}
