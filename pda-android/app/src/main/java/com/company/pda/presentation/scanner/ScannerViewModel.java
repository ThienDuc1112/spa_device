package com.company.pda.presentation.scanner;

import android.app.Application;
import androidx.lifecycle.*;
import com.company.pda.di.ScannerModule;
import com.company.pda.domain.scanner.ScannerRepository;
import com.company.pda.domain.usecase.ScanBarcodeUseCase;

public class ScannerViewModel extends AndroidViewModel {
  public final MutableLiveData<ScannerUiState> uiState =
      new MutableLiveData<>(new ScannerUiState(null, null));
  private ScannerRepository repository;
  private final ScanBarcodeUseCase scan = new ScanBarcodeUseCase();

  public ScannerViewModel(Application app) {
    super(app);
  }

  public void start() {
    if (repository == null) repository = ScannerModule.provide(getApplication());
    repository.start(
        r -> submit(r.barcode(), r.symbology()),
        message -> uiState.postValue(new ScannerUiState(null, message)));
  }

  public void submit(String barcode, String type) {
    try {
      uiState.setValue(new ScannerUiState(scan.execute(barcode, type), null));
    } catch (IllegalArgumentException e) {
      uiState.setValue(new ScannerUiState(null, e.getMessage()));
    }
  }

  public void consume() {
    uiState.setValue(new ScannerUiState(null, null));
  }

  public void trigger() {
    if (repository != null) repository.trigger();
  }

  public void stop() {
    if (repository != null) repository.stop();
  }

  public void reload() {
    stop();
    repository = null;
    start();
  }

  protected void onCleared() {
    stop();
  }
}
