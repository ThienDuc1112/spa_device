package com.company.pda.domain.repository;

import com.company.pda.domain.model.*;

public interface PdaFinderRepository {
  java.util.List<Device> devices() throws java.io.IOException;

  PdaFindRequest find(long deviceId) throws java.io.IOException;

  PdaFindRequest status(String id) throws java.io.IOException;

  void stop(String id) throws java.io.IOException;

  void register(String code, String name) throws java.io.IOException;

  void startLocal(String id, long expiresAtMillis);

  void stopLocal(String id);
}
