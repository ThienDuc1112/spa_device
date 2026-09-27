package com.company.pda.application.auth.usecase;

import com.company.pda.application.auth.dto.RefreshTokenCommand;
import com.company.pda.application.auth.dto.TokenResult;

public interface RefreshTokenUseCase {
  TokenResult refresh(RefreshTokenCommand body);
}
