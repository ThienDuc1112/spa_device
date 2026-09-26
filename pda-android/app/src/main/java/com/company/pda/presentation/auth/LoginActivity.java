package com.company.pda.presentation.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import com.company.pda.R;
import com.company.pda.common.util.*;
import com.company.pda.databinding.ActivityLoginBinding;
import com.company.pda.presentation.home.HomeActivity;

public class LoginActivity extends AlarmAwareActivity {
  public void onCreate(Bundle saved) {
    super.onCreate(saved);
    var vm = new ViewModelProvider(this).get(LoginViewModel.class);
    var binding = ActivityLoginBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    binding.setLifecycleOwner(this);
    binding.setVm(vm);
    ScreenSupport.insets(binding.getRoot());
    var ui = new ScreenViews(binding.content);
    ui.text("Sign in to your store", 18);
    var user = ui.input("Username", "", 1);
    user.setId(R.id.login_username);
    var password = ui.input("Password", "", 129);
    password.setId(R.id.login_password);
    ui.button("Sign in", () -> vm.login(user.getText().toString(), password.getText().toString()))
        .setId(R.id.login_submit);
    ScreenSupport.observe(this, this, vm);
    vm.uiState.observe(
        this,
        state -> {
          if (state.signedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
          }
        });
  }
}
