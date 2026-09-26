package com.company.pda.presentation.product;

import android.app.Application;
import androidx.lifecycle.*;
import com.company.pda.common.util.AsyncViewModel;
import com.company.pda.domain.usecase.GetProductUseCase;

public class ProductViewModel extends AsyncViewModel {
  public final MutableLiveData<ProductUiState> uiState =
      new MutableLiveData<>(new ProductUiState(null, false));

  public ProductViewModel(Application app) {
    super(app);
  }

  public void lookup(String barcode) {
    run(
        "Looking up product…",
        () -> {
          var result = new GetProductUseCase(modules.products).execute(barcode);
          return () -> {
            status.setValue(result.cached() ? "Offline • cached product" : "Product found");
            uiState.setValue(new ProductUiState(result.data(), result.cached()));
          };
        });
  }
}
