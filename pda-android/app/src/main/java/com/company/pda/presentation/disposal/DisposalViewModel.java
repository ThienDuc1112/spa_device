package com.company.pda.presentation.disposal;

import android.app.Application;
import androidx.lifecycle.*;
import com.company.pda.common.util.AsyncViewModel;
import com.company.pda.domain.usecase.ConfirmDisposalUseCase;
import java.math.BigDecimal;
import java.util.*;

public class DisposalViewModel extends AsyncViewModel {
  public final MutableLiveData<DisposalUiState> uiState =
      new MutableLiveData<>(new DisposalUiState(List.of(), null, 0));

  public DisposalViewModel(Application app) {
    super(app);
  }

  public void load(int page) {
    run(
        "Loading disposals…",
        () -> {
          var rows = modules.disposals.list(page);
          return () -> {
            status.setValue("Disposals • page " + (page + 1));
            uiState.setValue(new DisposalUiState(rows, null, page));
          };
        });
  }

  public void detail(String id) {
    run(
        "Loading disposal…",
        () -> {
          var row = modules.disposals.detail(id);
          return () -> {
            var current = uiState.getValue();
            status.setValue(row.disposal.status);
            uiState.setValue(new DisposalUiState(current.list(), row, current.page()));
          };
        });
  }

  public void create(String code, String quantity, String reason) {
    BigDecimal count;
    try {
      count = new BigDecimal(quantity);
    } catch (Exception e) {
      error.setValue("Enter a valid quantity");
      return;
    }
    String id = UUID.randomUUID().toString();
    run(
        "Creating disposal…",
        () -> {
          var d = modules.disposals.create(id, code, count, reason);
          var detail = modules.disposals.detail(d.id);
          return () -> {
            var current = uiState.getValue();
            status.setValue("Disposal created");
            uiState.setValue(new DisposalUiState(current.list(), detail, current.page()));
          };
        });
  }

  public void transition(boolean confirm) {
    var d = uiState.getValue().detail();
    if (d == null) return;
    String id = d.disposal.id;
    long version = d.disposal.version;
    run(
        confirm ? "Confirming disposal…" : "Cancelling disposal…",
        () -> {
          if (confirm) new ConfirmDisposalUseCase(modules.disposals).execute(id, version);
          else modules.disposals.transition(id, version, false);
          var row = modules.disposals.detail(id);
          return () -> {
            var current = uiState.getValue();
            status.setValue(row.disposal.status);
            uiState.setValue(new DisposalUiState(current.list(), row, current.page()));
          };
        });
  }
}
