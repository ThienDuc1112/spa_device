package com.company.pda.presentation.home;

import android.Manifest;
import android.content.*;
import android.os.*;
import android.widget.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.company.pda.R;
import com.company.pda.common.util.*;
import com.company.pda.databinding.ActivityHomeBinding;
import com.company.pda.presentation.disposal.DisposalFragment;
import com.company.pda.presentation.inventory.InventoryFragment;
import com.company.pda.presentation.pdafinder.PdaFinderActivity;
import com.company.pda.presentation.product.*;
import com.company.pda.presentation.scanner.ScannerViewModel;
import com.company.scanner.api.ScannerConfig;
import com.company.scanner.api.ScannerType;

public class HomeActivity extends AlarmAwareActivity {
  private HomeViewModel vm;
  private ActivityHomeBinding binding;

  public void onCreate(Bundle saved) {
    super.onCreate(saved);
    vm = new ViewModelProvider(this).get(HomeViewModel.class);
    if (!vm.loggedIn()) {
      ScreenSupport.login(this);
      return;
    }
    binding = ActivityHomeBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    binding.setLifecycleOwner(this);
    binding.setVm(vm);
    ScreenSupport.insets(binding.getRoot());
    ScreenSupport.observe(this, this, vm);
    var ui = new ScreenViews(binding.content);
    ui.button("Scan Order", () -> vm.navigate("product"));
    ui.button("Disposal inquiry", () -> vm.navigate("disposal"));
    ui.button("PDA management", () -> startActivity(new Intent(this, PdaFinderActivity.class)));
    ui.button("Register this PDA", this::register);
    ui.button(
        "Finder sound settings",
        () ->
            startActivity(
                new Intent(
                    this, com.company.pda.presentation.pdafinder.FinderSoundActivity.class)));
    ui.button("Scanner settings", this::scannerSettings);
    ui.button(
        "Sign out",
        () -> {
          vm.logout();
          ScreenSupport.login(this);
        });
    vm.screen.observe(this, this::navigate);
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new androidx.activity.OnBackPressedCallback(true) {
              public void handleOnBackPressed() {
                if ("product".equals(vm.screen.getValue())) finish();
                else vm.navigate("product");
              }
            });
    if (Build.VERSION.SDK_INT >= 33)
      registerForActivityResult(
              new ActivityResultContracts.RequestPermission(),
              allowed -> {
                if (!allowed) vm.status.setValue("Allow notifications to see finder alerts");
              })
          .launch(Manifest.permission.POST_NOTIFICATIONS);
  }

  private void navigate(String screen) {
    if (isFinishing()) return;
    var existing = getSupportFragmentManager().findFragmentById(R.id.feature_container);
    if (existing != null && screen.equals(existing.getTag())) return;
    Fragment fragment =
        switch (screen) {
          case "inventory" ->
              InventoryFragment.forProduct(vm.selected == null ? null : vm.selected.productCode);
          case "disposal" -> new DisposalFragment();
          case "image" ->
              ProductImageViewerFragment.forImage(
                  vm.selected == null ? null : vm.selected.imageUrl);
          default -> new ProductFragment();
        };
    binding.title.setText(
        switch (screen) {
          case "inventory" -> "Adjust inventory";
          case "disposal" -> "Disposals";
          case "image" -> "Product image";
          default -> "Scan order";
        });
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.feature_container, fragment, screen)
        .commit();
  }

  private void register() {
    if (vm.registered()) {
      new AlertDialog.Builder(this)
          .setMessage("This PDA is registered. Its push token refreshes automatically.")
          .setPositiveButton("OK", null)
          .show();
      return;
    }
    var box = ScreenViews.column(this);
    var ui = new ScreenViews(box);
    var code = ui.input("Unique asset code", "", 1);
    var name = ui.input("Device name", "", 1);
    new AlertDialog.Builder(this)
        .setTitle("Register this PDA (manager)")
        .setView(box)
        .setNegativeButton("Back", null)
        .setPositiveButton(
            "Register", (d, w) -> vm.register(code.getText().toString(), name.getText().toString()))
        .show();
  }

  private void scannerSettings() {
    var prefs = getSharedPreferences("scanner", MODE_PRIVATE);
    var box = ScreenViews.column(this);
    var ui = new ScreenViews(box);
    var types = new String[] {"KEYBOARD", "ZEBRA", "UROVO", "HONEYWELL", "INTENT", "CAMERA"};
    var type = new Spinner(this);
    type.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));
    box.addView(type);
    for (int i = 0; i < types.length; i++)
      if (types[i].equals(prefs.getString("type", "KEYBOARD"))) type.setSelection(i);
    var defaults =
        ScannerConfig.defaults(ScannerType.valueOf(types[type.getSelectedItemPosition()]));
    var action =
        ui.input("Configured broadcast action", prefs.getString("action", defaults.action()), 1);
    var extra = ui.input("Barcode payload key", prefs.getString("extra", defaults.dataExtra()), 1);
    var permission =
        ui.input("Vendor sender permission (if supported)", prefs.getString("permission", ""), 1);
    ui.button(
        "Use selected scanner defaults",
        () -> {
          var profile =
              ScannerConfig.defaults(ScannerType.valueOf(types[type.getSelectedItemPosition()]));
          action.setText(profile.action());
          extra.setText(profile.dataExtra());
          permission.setText("");
        });
    new AlertDialog.Builder(this)
        .setTitle("Scanner profile")
        .setView(box)
        .setNegativeButton("Back", null)
        .setPositiveButton(
            "Save",
            (d, w) -> {
              String selected = types[type.getSelectedItemPosition()];
              if (!selected.equals("CAMERA")
                  && !selected.equals("KEYBOARD")
                  && (action.getText().toString().isBlank()
                      || extra.getText().toString().isBlank())) {
                vm.status.setValue("Scanner action and payload key are required");
                return;
              }
              prefs
                  .edit()
                  .putString("type", selected)
                  .putString("action", action.getText().toString())
                  .putString("extra", extra.getText().toString())
                  .putString("permission", permission.getText().toString())
                  .apply();
              var scanner = new ViewModelProvider(this).get(ScannerViewModel.class);
              scanner.reload();
              if (!"product".equals(vm.screen.getValue())) scanner.stop();
              vm.status.setValue("Scanner profile saved");
            })
        .show();
  }
}
