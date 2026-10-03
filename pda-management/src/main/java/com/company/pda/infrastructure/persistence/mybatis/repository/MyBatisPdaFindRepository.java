package com.company.pda.infrastructure.persistence.mybatis.repository;

import com.company.pda.domain.pdafinder.model.PdaFindRequest;
import com.company.pda.domain.pdafinder.repository.PdaFindRepository;
import com.company.pda.infrastructure.persistence.mybatis.converter.PdaFindRequestEntityMapper;
import com.company.pda.infrastructure.persistence.mybatis.mapper.PdaFindMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisPdaFindRepository implements PdaFindRepository {
  private final PdaFindMapper mapper;

  public MyBatisPdaFindRepository(PdaFindMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public String lastPushEvent(long deviceId, long storeId) {
    return mapper.lastPushEvent(deviceId, storeId);
  }

  @Override
  public PdaFindRequest findAny(UUID id) {
    return PdaFindRequestEntityMapper.toDomain(mapper.findAny(id));
  }

  @Override
  public java.util.List<PdaFindRequest> commands(long deviceId, long storeId) {
    return mapper.commands(deviceId, storeId).stream()
        .map(PdaFindRequestEntityMapper::toDomain)
        .collect(java.util.stream.Collectors.toList());
  }

  @Override
  public PdaFindRequest find(UUID id, long storeId) {
    return PdaFindRequestEntityMapper.toDomain(mapper.find(id, storeId));
  }

  @Override
  public java.util.List<PdaFindRequest> expireForDevice(long deviceId, long storeId) {
    return mapper.expireForDevice(deviceId, storeId).stream()
        .map(PdaFindRequestEntityMapper::toDomain)
        .collect(java.util.stream.Collectors.toList());
  }

  @Override
  public int create(UUID id, Long actorId, long storeId, long deviceId, Instant expiresAt) {
    return mapper.create(id, actorId, storeId, deviceId, expiresAt);
  }

  @Override
  public int status(UUID id, String status) {
    return mapper.status(id, status);
  }

  @Override
  public int sent(UUID id) {
    return mapper.sent(id);
  }

  @Override
  public int log(UUID id, long deviceId, String event, String message) {
    return mapper.log(id, deviceId, event, message);
  }

  @Override
  public java.util.List<PdaFindRequest> expired() {
    return mapper.expired().stream()
        .map(PdaFindRequestEntityMapper::toDomain)
        .collect(java.util.stream.Collectors.toList());
  }
}
