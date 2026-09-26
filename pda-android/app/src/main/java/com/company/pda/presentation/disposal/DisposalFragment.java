package com.company.pda.presentation.disposal;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.company.pda.common.util.*;
import com.company.pda.presentation.home.HomeViewModel;

public class DisposalFragment extends Fragment {
  private DisposalViewModel vm;
  private HomeViewModel home;
  private ScreenViews ui;

  public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
    var box = ScreenViews.column(requireContext());
    ui = new ScreenViews(box);
    return box;
  }

  public void onViewCreated(@NonNull View view, Bundle saved) {
    var provider = new ViewModelProvider(requireActivity());
    vm = provider.get(DisposalViewModel.class);
    home = provider.get(HomeViewModel.class);
    ScreenSupport.observe((AppCompatActivity) requireActivity(), getViewLifecycleOwner(), vm);
    vm.uiState.observe(getViewLifecycleOwner(), this::render);
    if (vm.uiState.getValue().detail() == null) vm.load(vm.uiState.getValue().page());
  }

  private void render(DisposalUiState state) {
    ui.clear();
    if (state.detail() != null) {
      var d = state.detail();
      ui.text(d.disposal.remarks, 22);
      ui.text(d.disposal.status + "\n" + d.disposal.id, 14);
      for (var item : d.items)
        ui.text("Product #" + item.productId + " • " + item.quantity + " • " + item.reason, 16);
      ui.text("History", 18);
      for (var h : d.history) ui.text(h.toString(), 12);
      if (d.disposal.status.equals("PENDING")) {
        ui.button(
            "Confirm disposal",
            () ->
                new AlertDialog.Builder(requireContext())
                    .setTitle("Deduct inventory?")
                    .setMessage("Confirmation will deduct all disposal quantities.")
                    .setNegativeButton("Back", null)
                    .setPositiveButton("Confirm", (a, b) -> vm.transition(true))
                    .show());
        ui.button("Cancel disposal", () -> vm.transition(false));
      }
      ui.button("Refresh detail", () -> vm.detail(d.disposal.id));
      ui.button("Back to inquiry", () -> vm.load(state.page()));
      return;
    }
    if (home.selected != null) ui.button("Dispose " + home.selected.productCode, this::create);
    if (state.list().isEmpty()) ui.text("No disposals on this page", 18);
    for (var d : state.list()) ui.button(d.status + " · " + d.remarks, () -> vm.detail(d.id));
    if (state.page() > 0) ui.button("Previous page", () -> vm.load(state.page() - 1));
    if (state.list().size() == 50) ui.button("Next page", () -> vm.load(state.page() + 1));
    ui.button("Refresh", () -> vm.load(state.page()));
  }

  private void create() {
    String code = home.selected.productCode;
    var box = ScreenViews.column(requireContext());
    var fields = new ScreenViews(box);
    var qty = fields.input("Quantity", "", 8194);
    var reason = fields.input("Disposal reason", "", 1);
    new AlertDialog.Builder(requireContext())
        .setTitle("Dispose " + code)
        .setView(box)
        .setNegativeButton("Back", null)
        .setPositiveButton(
            "Create",
            (d, w) -> vm.create(code, qty.getText().toString(), reason.getText().toString()))
        .show();
  }

  public void onDestroyView() {
    ui = null;
    super.onDestroyView();
  }
}
