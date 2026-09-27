package com.company.pda.domain.inventory.exception;

import com.company.pda.domain.shared.exception.DomainException;

public class InsufficientInventoryException extends DomainException {
  public InsufficientInventoryException() {
    super(409, "Insufficient inventory");
  }
}
