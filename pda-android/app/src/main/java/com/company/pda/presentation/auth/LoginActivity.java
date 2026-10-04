package com.company.pda.presentation.auth;

import static com.company.pda.common.util.ApiCalls.execute;

import android.content.Intent;
import android.os.Bundle;
import com.company.pda.PdaApplication;
import com.company.pda.R;
import com.company.pda.common.util.*;
import com.company.pda.data.remote.dto.auth.AuthDto;
import com.company.pda.databinding.ActivityLoginBinding;
import com.company.pda.di.AppModule;
import com.company.pda.presentation.home.HomeActivity;

public class LoginActivity extends AlarmAwareActivity {
  private AppModule modules;
  private ScreenTasks tasks;

  public void onCreate(Bundle saved) {
    super.onCreate(saved);
    modules = ((PdaApplication) getApplication()).modules();
    tasks = new ScreenTasks(this, this);
    var binding = ActivityLoginBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    tasks.status.observe(this, binding.status::setText);
    ScreenSupport.insets(binding.getRoot());
    var ui = new ScreenViews(binding.content);
    ui.text("Sign in to your store", 18);
    var user = ui.input("Username", "", 1);
    user.setId(R.id.login_username);
    var password = ui.input("Password", "", 129);
    password.setId(R.id.login_password);
    ui.button("Sign in", () -> login(user.getText().toString(), password.getText().toString()))
        .setId(R.id.login_submit);
    ScreenSupport.observe(this, this, tasks);
    if (modules.tokens.tokens() != null) openHome();
  }

  private void openHome() {
    startActivity(new Intent(this, HomeActivity.class));
    finish();
  }

  private void login(String username, String password) {
    if (username.isBlank() || password.isBlank()) {
      tasks.error.setValue("Enter username and password");
      return;
    }
    tasks.run(
        "Signing in...",
        () -> {
          modules.tokens.tokens(
              execute(modules.authApi.login(new AuthDto.Login(username, password))));
          return () -> {
            tasks.clearRetry();
            openHome();
          };
        });
  }
}
