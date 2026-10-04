package com.company.pda.presentation.home;

import static com.company.pda.common.util.ApiCalls.execute;

import android.Manifest;
import android.content.*;
import android.os.*;
import android.widget.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.company.pda.PdaApplication;
import com.company.pda.R;
import com.company.pda.common.util.*;
import com.company.pda.data.remote.dto.pdafinder.PdaFinderDto;
import com.company.pda.databinding.ActivityHomeBinding;
import com.company.pda.di.AppModule;
import com.company.pda.domain.model.Product;
import com.company.pda.presentation.disposal.DisposalFragment;
import com.company.pda.presentation.inventory.InventoryFragment;
import com.company.pda.presentation.product.*;
import com.company.scanner.api.ScannerConfig;
import com.company.scanner.api.ScannerType;
import com.google.gson.Gson;
import java.util.*;

public class HomeActivity extends AlarmAwareActivity {
  private AppModule modules;
  public ScreenTasks tasks;
  public final ArrayList<Product> scanned = new ArrayList<>();
  public Product selected;
  public String screen = "product";
  private boolean registrationDialogOpen;
  private AlertDialog registrationDialog;
  private ActivityHomeBinding binding;

  @Override
  public void onResume() {
    super.onResume();
    ((com.company.pda.PdaApplication) getApplication()).modules().fcm.retryIfNeeded();
    if (tasks != null) checkRegistration();
    com.company.pda.infrastructure.firebase.FinderPollingService.start(this);
  }

  public void onCreate(Bundle saved) {
    super.onCreate(saved);
    modules = ((PdaApplication) getApplication()).modules();
    tasks = new ScreenTasks(this, this);
    if (saved != null) {
      screen = saved.getString("screen", "product");
      var rows = saved.getString("scanned");
      if (rows != null) scanned.addAll(Arrays.asList(new Gson().fromJson(rows, Product[].class)));
      selected = new Gson().fromJson(saved.getString("selected", "null"), Product.class);
    }
    if (!loggedIn()) {
      ScreenSupport.login(this);
      return;
    }
    binding = ActivityHomeBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    tasks.status.observe(this, binding.status::setText);
    ScreenSupport.insets(binding.getRoot());
    ScreenSupport.observe(this, this, tasks);
    var ui = new ScreenViews(binding.content);
    ui.button("Scan Order", () -> navigate("product"));
    ui.button("Disposal inquiry", () -> navigate("disposal"));
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
          modules.tokens.logout();
          ScreenSupport.login(this);
        });
    navigate(screen);
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new androidx.activity.OnBackPressedCallback(true) {
              public void handleOnBackPressed() {
                if ("product".equals(screen)) finish();
                else navigate("product");
              }
            });
    if (Build.VERSION.SDK_INT >= 33)
      registerForActivityResult(
              new ActivityResultContracts.RequestPermission(),
              allowed -> {
                if (!allowed) tasks.status.setValue("Allow notifications to see finder alerts");
              })
          .launch(Manifest.permission.POST_NOTIFICATIONS);
  }

  public void navigate(String screen) {
    this.screen = screen;
    if (isFinishing()) return;
    binding.title.setText(
        switch (screen) {
          case "inventory" -> "Adjust inventory";
          case "disposal" -> "Disposals";
          case "image" -> "Product image";
          default -> "Scan order";
        });
    var existing = getSupportFragmentManager().findFragmentById(R.id.feature_container);
    if (existing != null && screen.equals(existing.getTag())) return;
    Fragment fragment =
        switch (screen) {
          case "inventory" ->
              InventoryFragment.forProduct(selected == null ? null : selected.productCode);
          case "disposal" -> new DisposalFragment();
          case "image" ->
              ProductImageViewerFragment.forImage(selected == null ? null : selected.imageUrl);
          default -> new ProductFragment();
        };
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.feature_container, fragment, screen)
        .commit();
  }

  private void register() {
    if (modules.device.registered()) {
      new AlertDialog.Builder(this)
          .setMessage("This PDA is registered. Its push token refreshes automatically.")
          .setPositiveButton("OK", null)
          .show();
      return;
    }
    if (registrationDialogOpen) return;
    registrationDialogOpen = true;
    var box = ScreenViews.column(this);
    var ui = new ScreenViews(box);
    ui.text("Register this PDA in your account's store so it appears on the finder website.", 14);
    var code = ui.input("Unique asset code", suggestedDeviceCode(), 1);
    var name = ui.input("Device name", Build.MANUFACTURER + " " + Build.MODEL, 1);
    registrationDialog =
        new AlertDialog.Builder(this)
            .setTitle("Register this PDA")
            .setOnDismissListener(d -> registrationDialogOpen = false)
            .setView(box)
            .setNegativeButton("Back", null)
            .setPositiveButton(
                "Register",
                (d, w) -> register(code.getText().toString(), name.getText().toString()))
            .show();
  }

  @Override
  protected void onDestroy() {
    if (registrationDialog != null) registrationDialog.dismiss();
    super.onDestroy();
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
                tasks.status.setValue("Scanner action and payload key are required");
                return;
              }
              prefs
                  .edit()
                  .putString("type", selected)
                  .putString("action", action.getText().toString())
                  .putString("extra", extra.getText().toString())
                  .putString("permission", permission.getText().toString())
                  .apply();
              var current = getSupportFragmentManager().findFragmentById(R.id.feature_container);
              if (current instanceof ProductFragment) ((ProductFragment) current).reloadScanner();
              tasks.status.setValue("Scanner profile saved");
            })
        .show();
  }

  private boolean loggedIn() {
    return modules.tokens.tokens() != null;
  }

  public void select(Product product) {
    selected = product;
    if (scanned.stream().noneMatch(p -> p.barcode.equals(product.barcode))) scanned.add(product);
    if (scanned.size() > 200) scanned.remove(0);
    var current = getSupportFragmentManager().findFragmentById(R.id.feature_container);
    if (current instanceof ProductFragment) ((ProductFragment) current).render();
  }

  @Override
  public void onSaveInstanceState(Bundle saved) {
    saved.putString("screen", screen);
    saved.putString("scanned", new Gson().toJson(scanned));
    saved.putString("selected", new Gson().toJson(selected));
    super.onSaveInstanceState(saved);
  }

  public void checkRegistration() {
    if (!loggedIn()) return;
    if (!modules.device.registered()) {
      register();
      return;
    }
    String id = modules.tokens.get("deviceId"), secret = modules.tokens.get("deviceSecret");
    tasks.run(
        "Checking PDA registration…",
        () -> {
          try {
            // Device-authenticated, one check when Home resumes, not command/health polling.
            com.company.pda.common.util.ApiCalls.execute(
                modules.finderApi.fcmHealth(Long.parseLong(id), secret));
            return () -> {
              tasks.clearRetry();
              tasks.status.setValue("PDA registered");
            };
          } catch (com.company.pda.common.exception.ApiException e) {
            if (e.status != 401) throw e;
            return () -> {
              tasks.clearRetry();
              if (modules.device.clearRegistration(id, secret)) {
                getApplication()
                    .stopService(
                        new android.content.Intent(
                            getApplication(),
                            com.company.pda.infrastructure.firebase.FinderPollingService.class));
                getApplication()
                    .stopService(
                        new android.content.Intent(
                            getApplication(),
                            com.company.pda.infrastructure.alarm.PdaAlarmService.class));
                tasks.status.setValue("PDA registration was removed. Register this device again.");
                register();
              }
            };
          }
        });
  }

  public void register(String code, String name) {
    if (modules.device.registered()) return;
    if (code.trim().isEmpty()
        || name.trim().isEmpty()
        || code.trim().length() > 100
        || name.trim().length() > 255) {
      tasks.error.setValue(
          "Enter an asset code (up to 100 characters) and name (up to 255 characters)");
      return;
    }
    tasks.run(
        "Registering device…",
        () -> {
          var registration =
              execute(
                  modules.finderApi.register(
                      new PdaFinderDto.Register(
                          code.trim(), name.trim(), modules.tokens.get("fcmToken"))));
          modules.device.registered(registration.deviceId, registration.deviceSecret);
          return () -> {
            tasks.clearRetry();
            modules.fcm.initialize();
            com.company.pda.infrastructure.firebase.FinderPollingService.start(getApplication());
            tasks.status.setValue("Device registered");
          };
        });
  }

  public String suggestedDeviceCode() {
    String code = modules.tokens.get("registrationCode");
    if (code == null) {
      code = "PDA-" + UUID.randomUUID();
      modules.tokens.put("registrationCode", code);
    }
    return code;
  }
}
