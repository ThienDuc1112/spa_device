package com.company.pda.presentation.inventory;

import static com.company.pda.common.util.ApiCalls.execute;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.company.pda.PdaApplication;
import com.company.pda.common.util.*;
import com.company.pda.data.remote.dto.inventory.InventoryDto;
import com.company.pda.di.AppModule;
import com.company.pda.presentation.home.HomeActivity;
import java.math.BigDecimal;
import java.util.UUID;

public class InventoryFragment extends Fragment {
  private ScreenTasks tasks;
  private AppModule modules;
  private InventoryDto row;
  private HomeActivity home;
  private ScreenViews ui;

  public static InventoryFragment forProduct(String code) {
    var f = new InventoryFragment();
    var args = new Bundle();
    args.putString("productCode", code);
    f.setArguments(args);
    return f;
  }

  public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
    var box = ScreenViews.column(requireContext());
    ui = new ScreenViews(box);
    return box;
  }

  public void onViewCreated(@NonNull View view, Bundle saved) {
    home = (HomeActivity) requireActivity();
    modules = ((PdaApplication) home.getApplication()).modules();
    tasks = new ScreenTasks(requireContext(), getViewLifecycleOwner());
    ScreenSupport.observe(home, getViewLifecycleOwner(), tasks);
    load();
  }

  private void load() {
    String code = getArguments() == null ? null : getArguments().getString("productCode");
    if (code == null) {
      render();
      return;
    }
    tasks.run(
        "Loading inventory...",
        () -> {
          var result = execute(modules.inventoryApi.inventory(code));
          return () -> {
            tasks.clearRetry();
            row = result;
            render();
          };
        });
  }

  private void adjust(String quantity, String reason) {
    if (row == null) return;
    final BigDecimal count;
    try {
      count = new BigDecimal(quantity);
    } catch (Exception e) {
      tasks.error.setValue("Enter a valid quantity");
      return;
    }
    var body =
        new InventoryDto.Adjustment(
            UUID.randomUUID().toString(),
            getArguments().getString("productCode"),
            count,
            row.version,
            reason);
    tasks.run(
        "Saving inventory...",
        () -> {
          var updated = execute(modules.inventoryApi.adjust(body));
          return () -> {
            tasks.clearRetry();
            home.tasks.status.setValue("Inventory saved - " + updated.quantity);
            home.navigate("product");
          };
        });
  }

  private void render() {
    ui.clear();
    if (row == null) {
      ui.text("Select a product in Scan Order", 18);
      return;
    }
    ui.text(getArguments().getString("productCode"), 22);
    ui.text("Current quantity: " + row.quantity + " • version " + row.version, 16);
    var qty = ui.input("New quantity", row.quantity.toPlainString(), 8194);
    var reason = ui.input("Reason", "", 1);
    ui.button(
        "Save adjustment", () -> adjust(qty.getText().toString(), reason.getText().toString()));
    ui.button("Reload current stock", this::load);
  }

  public void onDestroyView() {
    ui = null;
    super.onDestroyView();
  }
}
