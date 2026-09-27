package com.company.pda.application.auth.usecase;

import com.company.pda.application.auth.dto.RegisterUserCommand;
import com.company.pda.application.auth.dto.RegisterUserResult;

public interface RegisterUserUseCase {
  RegisterUserResult register(RegisterUserCommand command);
}
