package com.company.pda.presentation.home;

import android.app.Application;
import androidx.lifecycle.*;
import com.company.pda.common.util.AsyncViewModel;
import com.company.pda.domain.model.Product;
import com.google.gson.Gson;
import java.util.*;

/** Activity-scoped Scan Order state survives fragment changes and process recreation. */
public class HomeViewModel extends AsyncViewModel {
  private final SavedStateHandle saved;
  public final MutableLiveData<String> screen;
  public final MutableLiveData<Integer> revision = new MutableLiveData<>(0);
  public final MutableLiveData<Boolean> registrationRequired = new MutableLiveData<>(false);
  public final ArrayList<Product> scanned = new ArrayList<>();
  public Product selected;

  public HomeViewModel(Application app, SavedStateHandle saved) {
    super(app);
    this.saved = saved;
    screen = saved.getLiveData("screen", "product");
    String rows = saved.get("scanned");
    if (rows != null) scanned.addAll(Arrays.asList(new Gson().fromJson(rows, Product[].class)));
    String chosen = saved.get("selected");
    if (chosen != null) selected = new Gson().fromJson(chosen, Product.class);
  }

  public boolean loggedIn() {
    return modules.auth.loggedIn();
  }

  public void select(Product product) {
    selected = product;
    if (scanned.stream().noneMatch(p -> p.barcode.equals(product.barcode))) scanned.add(product);
    if (scanned.size() > 200) scanned.remove(0);
    saved.set("scanned", new Gson().toJson(scanned));
    saved.set("selected", new Gson().toJson(selected));
    revision.setValue(revision.getValue() + 1);
  }

  public void navigate(String destination) {
    screen.setValue(destination);
  }

  public void logout() {
    modules.auth.logout();
    scanned.clear();
    selected = null;
    saved.set("scanned", null);
    saved.set("selected", null);
    navigate("product");
  }

  public boolean registered() {
    return modules.device.registered();
  }

  public void checkRegistration() {
    if (!loggedIn()) return;
    if (!registered()) {
      registrationRequired.setValue(true);
      return;
    }
    String id = modules.tokens.get("deviceId"), secret = modules.tokens.get("deviceSecret");
    run(
        "Checking PDA registration…",
        () -> {
          try {
            // Device-authenticated, one check when Home resumes, not command/health polling.
            com.company.pda.common.util.ApiCalls.execute(
                modules.finderApi.fcmHealth(Long.parseLong(id), secret));
            return () -> {
              clearRetry();
              status.setValue("PDA registered");
            };
          } catch (com.company.pda.common.exception.ApiException e) {
            if (e.status != 401) throw e;
            return () -> {
              clearRetry();
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
                status.setValue("PDA registration was removed. Register this device again.");
                registrationRequired.setValue(true);
              }
            };
          }
        });
  }

  public void register(String code, String name) {
    if (registered()) return;
    if (code.trim().isEmpty()
        || name.trim().isEmpty()
        || code.trim().length() > 100
        || name.trim().length() > 255) {
      error.setValue("Enter an asset code (up to 100 characters) and name (up to 255 characters)");
      return;
    }
    run(
        "Registering device…",
        () -> {
          modules.finder.register(code.trim(), name.trim());
          return () -> {
            clearRetry();
            modules.fcm.initialize();
            com.company.pda.infrastructure.firebase.FinderPollingService.start(getApplication());
            status.setValue("Device registered");
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
