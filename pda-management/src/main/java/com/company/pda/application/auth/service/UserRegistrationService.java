package com.company.pda.application.auth.service;

import com.company.pda.application.auth.dto.RegisterUserCommand;
import com.company.pda.application.auth.dto.RegisterUserResult;
import com.company.pda.application.auth.usecase.RegisterUserUseCase;
import com.company.pda.application.port.out.AuditLogPort;
import com.company.pda.application.port.out.CurrentActor;
import com.company.pda.application.port.out.PasswordHasher;
import com.company.pda.domain.auth.repository.AuthRepository;
import com.company.pda.domain.shared.exception.DomainException;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRegistrationService implements RegisterUserUseCase {
  private final AuthRepository users;
  private final CurrentActor actor;
  private final PasswordHasher passwords;
  private final AuditLogPort audit;

  public UserRegistrationService(
      AuthRepository users, CurrentActor actor, PasswordHasher passwords, AuditLogPort audit) {
    this.users = users;
    this.actor = actor;
    this.passwords = passwords;
    this.audit = audit;
  }

  @Override
  @Transactional
  public RegisterUserResult register(RegisterUserCommand command) {
    var manager = actor.get();
    if (!users.roles(manager.id()).contains("MANAGER")) {
      throw new DomainException(403, "Only managers may register employees");
    }
    if (command.password().length() < 12
        || command.password().getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new DomainException(
          400, "Password must have at least 12 characters and at most 72 UTF-8 bytes");
    }
    if (users.user(command.username()) != null) {
      throw new DomainException(409, "Username already exists");
    }
    long id =
        users.createUser(
            command.username(),
            passwords.encode(command.password()),
            command.fullName(),
            command.email(),
            manager.storeId());
    if (users.assignRole(id, "EMPLOYEE") != 1) {
      throw new IllegalStateException("EMPLOYEE role is not configured");
    }
    audit.audit(manager.id(), manager.storeId(), "USER_REGISTER", Long.toString(id));
    return new RegisterUserResult(id, command.username(), manager.storeId(), "EMPLOYEE");
  }
}
