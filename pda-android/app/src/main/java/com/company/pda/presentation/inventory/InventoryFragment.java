package com.company.pda.presentation.inventory;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.company.pda.common.util.*;
import com.company.pda.presentation.home.HomeViewModel;

public class InventoryFragment extends Fragment {
  private InventoryViewModel vm;
  private HomeViewModel home;
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
    var provider = new ViewModelProvider(requireActivity());
    vm = provider.get(InventoryViewModel.class);
    home = provider.get(HomeViewModel.class);
    ScreenSupport.observe((AppCompatActivity) requireActivity(), getViewLifecycleOwner(), vm);
    vm.uiState.observe(
        getViewLifecycleOwner(),
        state -> {
          if (state.saved()) {
            home.status.setValue("Inventory saved • " + state.inventory().quantity);
            vm.acknowledgeSave();
            home.navigate("product");
            return;
          }
          render(state);
        });
    vm.load(getArguments() == null ? null : getArguments().getString("productCode"));
  }

  private void render(InventoryUiState state) {
    ui.clear();
    if (state.inventory() == null) {
      ui.text("Select a product in Scan Order", 18);
      return;
    }
    var row = state.inventory();
    ui.text(getArguments().getString("productCode"), 22);
    ui.text("Current quantity: " + row.quantity + " • version " + row.version, 16);
    var qty = ui.input("New quantity", row.quantity.toPlainString(), 8194);
    var reason = ui.input("Reason", "", 1);
    ui.button(
        "Save adjustment", () -> vm.adjust(qty.getText().toString(), reason.getText().toString()));
    ui.button("Reload current stock", () -> vm.load(getArguments().getString("productCode")));
  }

  public void onDestroyView() {
    ui = null;
    super.onDestroyView();
  }
}
