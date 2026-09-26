package com.company.pda.presentation.auth;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;
import com.company.pda.common.util.AsyncViewModel;
import com.company.pda.domain.usecase.LoginUseCase;

public class LoginViewModel extends AsyncViewModel {
  public final MutableLiveData<LoginUiState> uiState;

  public LoginViewModel(Application app) {
    super(app);
    uiState = new MutableLiveData<>(new LoginUiState(modules.auth.loggedIn()));
  }

  public void login(String username, String password) {
    if (username.isBlank() || password.isBlank()) {
      error.setValue("Enter username and password");
      return;
    }
    run(
        "Signing in…",
        () -> {
          new LoginUseCase(modules.auth).execute(username, password);
          return () -> {
            clearRetry();
            status.setValue("Signed in");
            uiState.setValue(new LoginUiState(true));
          };
        });
  }
}
