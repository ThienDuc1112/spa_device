package com.company.pda.presentation.inventory;

import android.app.Application;
import androidx.lifecycle.*;
import com.company.pda.common.util.AsyncViewModel;
import com.company.pda.domain.usecase.AdjustInventoryUseCase;
import java.math.BigDecimal;
import java.util.UUID;

public class InventoryViewModel extends AsyncViewModel {
  public final MutableLiveData<InventoryUiState> uiState =
      new MutableLiveData<>(new InventoryUiState(null, false));
  private String code;

  public InventoryViewModel(Application app) {
    super(app);
  }

  public void load(String code) {
    if (code == null) return;
    this.code = code;
    uiState.setValue(new InventoryUiState(null, false));
    run(
        "Loading inventory…",
        () -> {
          var row = modules.inventory.get(code);
          return () -> {
            status.setValue("Current stock loaded");
            uiState.setValue(new InventoryUiState(row, false));
          };
        });
  }

  public void adjust(String quantity, String reason) {
    var state = uiState.getValue();
    if (state == null || state.inventory() == null) return;
    BigDecimal count;
    try {
      count = new BigDecimal(quantity);
    } catch (Exception e) {
      error.setValue("Enter a valid quantity");
      return;
    }
    String id = UUID.randomUUID().toString(), productCode = code;
    long version = state.inventory().version;
    run(
        "Saving inventory…",
        () -> {
          var row =
              new AdjustInventoryUseCase(modules.inventory)
                  .execute(id, productCode, count, version, reason);
          return () -> {
            status.setValue("Inventory saved");
            uiState.setValue(new InventoryUiState(row, true));
          };
        });
  }

  public void acknowledgeSave() {
    var value = uiState.getValue();
    if (value != null) uiState.setValue(new InventoryUiState(value.inventory(), false));
  }
}
