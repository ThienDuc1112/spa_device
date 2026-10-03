package com.company.pda.domain.repository;

public interface PdaFinderRepository {
  void register(String code, String name) throws java.io.IOException;

  void startLocal(String id, long expiresAtMillis);

  void stopLocal(String id);
}
