package com.company.pda.presentation.product;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.company.pda.R;
import com.company.pda.common.util.*;
import com.company.pda.presentation.home.HomeViewModel;
import com.company.pda.presentation.scanner.ScannerViewModel;

public class ProductFragment extends Fragment {
  private HomeViewModel home;
  private ProductViewModel vm;
  private ScannerViewModel scanner;
  private ScreenViews ui;
  private TextView status;

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
    var box = ScreenViews.column(requireContext());
    ui = new ScreenViews(box);
    return box;
  }

  public void onViewCreated(@NonNull View view, Bundle saved) {
    var provider = new ViewModelProvider(requireActivity());
    home = provider.get(HomeViewModel.class);
    vm = provider.get(ProductViewModel.class);
    scanner = provider.get(ScannerViewModel.class);
    ScreenSupport.observe((AppCompatActivity) requireActivity(), getViewLifecycleOwner(), vm);
    home.revision.observe(getViewLifecycleOwner(), r -> render());
    vm.status.observe(
        getViewLifecycleOwner(),
        value -> {
          if (status != null) status.setText(value);
        });
    vm.uiState.observe(
        getViewLifecycleOwner(),
        state -> {
          if (state.product() != null) {
            home.select(state.product());
            vm.uiState.setValue(new ProductUiState(null, state.cached()));
          }
        });
    scanner.uiState.observe(
        getViewLifecycleOwner(),
        state -> {
          if (state.result() != null) {
            var result = state.result();
            scanner.consume();
            vm.lookup(result.barcode());
          } else if (state.error() != null) {
            vm.error.setValue(state.error());
            scanner.consume();
          }
        });
    render();
  }

  public void onResume() {
    super.onResume();
    scanner.start();
  }

  public void onPause() {
    scanner.stop();
    super.onPause();
  }

  private void render() {
    ui.clear();
    status = ui.text(vm.status.getValue(), 14);
    var barcode = ui.input("Scan or enter barcode", "", 1);
    barcode.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
    barcode.setOnEditorActionListener(
        (v, a, event) -> {
          if (a == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
              || (event != null
                  && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                  && event.getAction() == KeyEvent.ACTION_DOWN)) {
            scanner.submit(barcode.getText().toString(), "KEYBOARD");
            return true;
          }
          return false;
        });
    ui.button("Look up product", () -> scanner.submit(barcode.getText().toString(), "MANUAL"));
    ui.button("Trigger configured scanner", scanner::trigger);
    if (home.selected != null) {
      var p = home.selected;
      ui.text(p.productName, 22);
      ui.text(p.productCode + " • " + p.barcode, 14);
      var image = new ImageView(requireContext());
      image.setContentDescription("Product image");
      ui.content.addView(image, new LinearLayout.LayoutParams(-1, 400));
      Glide.with(this)
          .load(p.imageUrl)
          .placeholder(R.drawable.product_placeholder)
          .error(R.drawable.product_placeholder)
          .fallback(R.drawable.product_placeholder)
          .into(image);
      ui.button("View image / zoom", () -> home.navigate("image"));
      ui.button("Inventory Adjustment", () -> home.navigate("inventory"));
      ui.button("Dispose selected product", () -> home.navigate("disposal"));
    }
    ui.text("Scanned items (" + home.scanned.size() + ")", 18);
    for (var p : home.scanned)
      ui.button(p.productCode + " · " + p.productName, () -> home.select(p));
  }

  public void onDestroyView() {
    ui = null;
    status = null;
    super.onDestroyView();
  }
}
