package com.company.pda.presentation.pdafinder;

import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import com.company.pda.PdaApplication;
import com.company.pda.common.util.*;
import com.company.pda.databinding.ActivityPdaFinderBinding;

public class PdaFinderActivity extends AlarmAwareActivity {
  public void onCreate(Bundle saved) {
    super.onCreate(saved);
    if (!((PdaApplication) getApplication()).modules().auth.loggedIn()) {
      ScreenSupport.login(this);
      return;
    }
    var vm = new ViewModelProvider(this).get(PdaFinderViewModel.class);
    var binding = ActivityPdaFinderBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    binding.setLifecycleOwner(this);
    binding.setVm(vm);
    ScreenSupport.insets(binding.getRoot());
    ScreenSupport.observe(this, this, vm);
    var ui = new ScreenViews(binding.content);
    vm.uiState.observe(
        this,
        state -> {
          ui.clear();
          ui.text("Refresh to check whether the device acknowledged the alert.", 14);
          ui.text(
              "RINGING means playback passed device audio checks; it does not confirm that the"
                  + " speaker was heard.",
              14);
          ui.button(
              "Finder sound settings on this PDA",
              () -> startActivity(new android.content.Intent(this, FinderSoundActivity.class)));
          for (var d : state.devices())
            ui.button(
                d.deviceName + " · " + (d.reachable ? "Find" : "No push token"),
                () -> vm.find(d.id));
          if (state.request() != null) {
            ui.text("Request " + state.request().id + "\n" + state.request().status, 16);
            ui.button("Refresh finder status", vm::refresh);
            ui.button("Stop alarm", vm::stop);
          }
          ui.button("Refresh devices", vm::load);
          ui.button("Back", this::finish);
        });
    vm.load();
  }
}
