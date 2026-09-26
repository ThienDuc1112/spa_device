package com.company.pda.domain.repository;

import com.company.pda.domain.model.*;

public interface DisposalRepository {
  java.util.List<Disposal> list(int page) throws java.io.IOException;

  DisposalDetail detail(String id) throws java.io.IOException;

  Disposal create(String requestId, String code, java.math.BigDecimal quantity, String reason)
      throws java.io.IOException;

  Disposal transition(String id, long version, boolean confirm) throws java.io.IOException;
}
