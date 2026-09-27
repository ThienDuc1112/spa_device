package com.company.pda.application.pdafinder.usecase;

import com.company.pda.application.pdafinder.dto.FindPdaCommand;
import com.company.pda.application.pdafinder.dto.FindPdaResult;

public interface FindPdaUseCase {
  FindPdaResult find(FindPdaCommand command);
}
