package com.company.application;

import com.company.application.dto.Contracts.*;

public interface AuthUseCase {
  Tokens login(Login body);

  Tokens refresh(RefreshBody body);
}
