package com.company.pda.presentation.disposal;

import static com.company.pda.common.util.ApiCalls.execute;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.*;
import androidx.fragment.app.Fragment;
import com.company.pda.PdaApplication;
import com.company.pda.common.util.*;
import com.company.pda.data.remote.dto.disposal.DisposalDto;
import com.company.pda.di.AppModule;
import com.company.pda.presentation.home.HomeActivity;
import java.math.BigDecimal;
import java.util.*;

public class DisposalFragment extends Fragment {
  private ScreenTasks tasks;
  private AppModule modules;
  private DisposalUiState state = new DisposalUiState(List.of(), null, 0);
  private HomeActivity home;
  private ScreenViews ui;

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
    String detailId = state.detail() == null ? null : state.detail().disposal.id;
    if (saved != null) {
      state = new DisposalUiState(List.of(), null, saved.getInt("disposalPage", 0));
      detailId = saved.getString("disposalId");
    }
    render(state);
    if (detailId == null) load(state.page());
    else detail(detailId);
  }

  @Override
  public void onSaveInstanceState(Bundle saved) {
    saved.putInt("disposalPage", state.page());
    if (state.detail() != null) saved.putString("disposalId", state.detail().disposal.id);
    super.onSaveInstanceState(saved);
  }

  private void load(int page) {
    tasks.run(
        "Loading disposals...",
        () -> {
          var rows = execute(modules.disposalsApi.list(page));
          return () -> {
            tasks.clearRetry();
            render(new DisposalUiState(rows, null, page));
          };
        });
  }

  private void detail(String id) {
    tasks.run(
        "Loading disposal...",
        () -> {
          var row = execute(modules.disposalsApi.detail(id));
          return () -> {
            tasks.clearRetry();
            render(new DisposalUiState(state.list(), row, state.page()));
          };
        });
  }

  private void create(String code, String quantity, String reason) {
    final BigDecimal count;
    try {
      count = new BigDecimal(quantity);
    } catch (Exception e) {
      tasks.error.setValue("Enter a valid quantity");
      return;
    }
    var body =
        new DisposalDto.Create(
            UUID.randomUUID().toString(),
            reason,
            List.of(new DisposalDto.Line(code, count, reason)));
    tasks.run(
        "Creating disposal...",
        () -> {
          var created = execute(modules.disposalsApi.create(body));
          var row = execute(modules.disposalsApi.detail(created.id));
          return () -> {
            tasks.clearRetry();
            render(new DisposalUiState(state.list(), row, state.page()));
          };
        });
  }

  private void transition(boolean confirm) {
    if (state.detail() == null) return;
    String id = state.detail().disposal.id;
    var body = new DisposalDto.Transition(state.detail().disposal.version);
    tasks.run(
        confirm ? "Confirming disposal..." : "Cancelling disposal...",
        () -> {
          execute(
              confirm
                  ? modules.disposalsApi.confirm(id, body)
                  : modules.disposalsApi.cancel(id, body));
          var row = execute(modules.disposalsApi.detail(id));
          return () -> {
            tasks.clearRetry();
            render(new DisposalUiState(state.list(), row, state.page()));
          };
        });
  }

  private void render(DisposalUiState state) {
    this.state = state;
    ui.clear();
    if (state.detail() != null) {
      var d = state.detail();
      ui.text(d.disposal.remarks, 22);
      ui.text(d.disposal.status + "\n" + d.disposal.id, 14);
      for (var item : d.items)
        ui.text("Product #" + item.productId + " â€¢ " + item.quantity + " â€¢ " + item.reason, 16);
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
                    .setPositiveButton("Confirm", (a, b) -> transition(true))
                    .show());
        ui.button("Cancel disposal", () -> transition(false));
      }
      ui.button("Refresh detail", () -> detail(d.disposal.id));
      ui.button("Back to inquiry", () -> load(state.page()));
      return;
    }
    if (home.selected != null) ui.button("Dispose " + home.selected.productCode, this::create);
    if (state.list().isEmpty()) ui.text("No disposals on this page", 18);
    for (var d : state.list()) ui.button(d.status + " Â· " + d.remarks, () -> detail(d.id));
    if (state.page() > 0) ui.button("Previous page", () -> load(state.page() - 1));
    if (state.list().size() == 50) ui.button("Next page", () -> load(state.page() + 1));
    ui.button("Refresh", () -> load(state.page()));
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
            "Create", (d, w) -> create(code, qty.getText().toString(), reason.getText().toString()))
        .show();
  }

  public void onDestroyView() {
    ui = null;
    super.onDestroyView();
  }
}
