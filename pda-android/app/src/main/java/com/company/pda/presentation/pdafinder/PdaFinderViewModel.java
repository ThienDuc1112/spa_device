package com.company.pda.presentation.pdafinder;

import android.app.Application;
import androidx.lifecycle.*;
import com.company.pda.common.util.AsyncViewModel;
import java.util.List;

public class PdaFinderViewModel extends AsyncViewModel {
  public final MutableLiveData<PdaFinderUiState> uiState =
      new MutableLiveData<>(new PdaFinderUiState(List.of(), null));

  public PdaFinderViewModel(Application app) {
    super(app);
  }

  public void load() {
    run(
        "Loading devices…",
        () -> {
          var rows = modules.finder.devices();
          return () -> {
            var current = uiState.getValue();
            uiState.setValue(new PdaFinderUiState(rows, current.request()));
            status.setValue("Select a PDA to locate");
          };
        });
  }

  public void find(long id) {
    run(
        "Sending finder request…",
        () -> {
          var row = modules.finder.find(id);
          return () -> {
            uiState.setValue(new PdaFinderUiState(uiState.getValue().devices(), row));
            status.setValue("Finder: " + row.status);
          };
        });
  }

  public void refresh() {
    var request = uiState.getValue().request();
    if (request == null) return;
    run(
        "Checking delivery…",
        () -> {
          var row = modules.finder.status(request.id);
          return () -> {
            uiState.setValue(new PdaFinderUiState(uiState.getValue().devices(), row));
            status.setValue("Finder: " + row.status);
          };
        });
  }

  public void stop() {
    var request = uiState.getValue().request();
    if (request == null) return;
    run(
        "Stopping alarm…",
        () -> {
          modules.finder.stop(request.id);
          return () -> {
            uiState.setValue(new PdaFinderUiState(uiState.getValue().devices(), null));
            status.setValue("Stop requested");
          };
        });
  }
}
