package com.company.pda.infrastructure.persistence.mybatis.repository;

import com.company.pda.domain.device.model.Device;
import com.company.pda.domain.device.repository.DeviceRepository;
import com.company.pda.infrastructure.persistence.mybatis.converter.DeviceEntityMapper;
import com.company.pda.infrastructure.persistence.mybatis.mapper.DeviceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisDeviceRepository implements DeviceRepository {
  private final DeviceMapper mapper;

  public MyBatisDeviceRepository(DeviceMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Device lock(long id) {
    return DeviceEntityMapper.toDomain(mapper.lock(id));
  }

  @Override
  public boolean hasActiveFinder(long id) {
    return mapper.hasActiveFinder(id);
  }

  @Override
  public void delete(long id) {
    mapper.deleteFinderOutbox(id);
    mapper.deleteFinderLogs(id);
    mapper.deleteFinderRequests(id);
    mapper.delete(id);
  }

  @Override
  public Device find(long id, long storeId) {
    return DeviceEntityMapper.toDomain(mapper.find(id, storeId));
  }

  @Override
  public Device identity(long id) {
    return DeviceEntityMapper.toDomain(mapper.identity(id));
  }

  @Override
  public java.util.List<Device> list(long storeId) {
    return mapper.list(storeId).stream()
        .map(DeviceEntityMapper::toDomain)
        .collect(java.util.stream.Collectors.toList());
  }

  @Override
  public Long register(
      String code, String name, long storeId, long actorId, String hash, String token) {
    return mapper.register(code, name, storeId, actorId, hash, token);
  }

  @Override
  public int token(long id, String token) {
    return mapper.token(id, token);
  }

  @Override
  public int invalidate(long id, String token) {
    return mapper.invalidate(id, token);
  }
}
