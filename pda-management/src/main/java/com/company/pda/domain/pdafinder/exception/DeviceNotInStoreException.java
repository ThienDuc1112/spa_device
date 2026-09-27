package com.company.pda.domain.pdafinder.exception;

import com.company.pda.domain.shared.exception.DomainException;

public class DeviceNotInStoreException extends DomainException {
  public DeviceNotInStoreException() {
    super(404, "Resource not found");
  }
}
