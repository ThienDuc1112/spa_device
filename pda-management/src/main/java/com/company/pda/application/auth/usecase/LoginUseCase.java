package com.company.pda.application.auth.usecase;

import com.company.pda.application.auth.dto.LoginCommand;
import com.company.pda.application.auth.dto.TokenResult;

public interface LoginUseCase {
  TokenResult login(LoginCommand body);
}
