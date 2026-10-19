package com.company.pda.presentation.product;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.company.pda.PdaApplication;
import com.company.pda.R;
import com.company.pda.common.exception.ApiException;
import com.company.pda.common.util.*;
import com.company.pda.data.local.entity.ProductCache;
import com.company.pda.data.mapper.ProductMapper;
import com.company.pda.di.AppModule;
import com.company.pda.di.ScannerModule;
import com.company.pda.presentation.home.HomeActivity;
import com.company.scanner.api.ScanCallback;
import com.company.scanner.api.ScanResult;
import com.company.scanner.api.ScannerManager;
import java.util.concurrent.CompletionException;

public class ProductFragment extends Fragment {
  private HomeActivity home;
  private AppModule modules;
  private ScreenTasks tasks;
  private ScannerManager scanner;
  private int scannerSession;
  private ScreenViews ui;
  private TextView status;

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
    var box = ScreenViews.column(requireContext());
    ui = new ScreenViews(box);
    return box;
  }

  public void onViewCreated(@NonNull View view, Bundle saved) {
    home = (HomeActivity) requireActivity();
    modules = ((PdaApplication) home.getApplication()).modules();
    tasks = new ScreenTasks(requireContext(), getViewLifecycleOwner());
    ScreenSupport.observe(home, getViewLifecycleOwner(), tasks);
    tasks.status.observe(
        getViewLifecycleOwner(),
        value -> {
          if (status != null) status.setText(value);
        });
    render();
  }

  @Override
  public void onResume() {
    super.onResume();
    reloadScanner();
  }

  public void reloadScanner() {
    int session = ++scannerSession;
    if (scanner != null) scanner.stop();
    if (!isResumed()) return;
    scanner = ScannerModule.provide(requireContext());
    scanner.start(
        new ScanCallback() {
          public void onScan(ScanResult result) {
            new android.os.Handler(android.os.Looper.getMainLooper())
                .post(
                    () -> {
                      if (session == scannerSession && ui != null && isResumed())
                        lookup(result.barcode());
                    });
          }

          public void onError(String message) {
            new android.os.Handler(android.os.Looper.getMainLooper())
                .post(
                    () -> {
                      if (session == scannerSession && ui != null && isResumed())
                        tasks.error.setValue(message);
                    });
          }
        });
  }

  @Override
  public void onPause() {
    scannerSession++;
    if (scanner != null) scanner.stop();
    super.onPause();
  }

  private void lookup(String input) {
    final String barcode;
    try {
      barcode = BarcodeInput.normalize(input);
    } catch (IllegalArgumentException e) {
      tasks.error.setValue(e.getMessage());
      return;
    }
    HttpReceiver<LookupResult> httpReceiver =
        result -> {
          tasks.clearRetry();
          tasks.status.setValue(result.cached ? "Offline - cached product" : "Product found");
          home.select(result.product);
        };
    tasks.runAsync(
        "Looking up product...",
        () ->
            modules
                .productsApi
                .product(barcode)
                .handleAsync(
                    (response, failure) -> {
                      if (failure == null) {
                        var product = new ProductMapper().toDomain(response);
                        modules.database.products().cache(ProductCache.from(product));
                        return new LookupResult(product, false);
                      }
                      Throwable cause = failure;
                      while (cause instanceof CompletionException && cause.getCause() != null)
                        cause = cause.getCause();
                      if (cause instanceof java.io.IOException
                          && !(cause instanceof ApiException)) {
                        var row = modules.database.products().product(barcode);
                        if (row != null) return new LookupResult(row.product(), true);
                      }
                      throw new CompletionException(cause);
                    },
                    modules.io),
        httpReceiver);
  }

  private static final class LookupResult {
    final com.company.pda.domain.model.Product product;
    final boolean cached;

    LookupResult(com.company.pda.domain.model.Product product, boolean cached) {
      this.product = product;
      this.cached = cached;
    }
  }

  public void render() {
    if (ui == null) return;
    ui.clear();
    status = ui.text(tasks.status.getValue(), 14);
    var barcode = ui.input("Scan or enter barcode", "", 1);
    barcode.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
    barcode.setOnEditorActionListener(
        (v, a, event) -> {
          if (a == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
              || (event != null
                  && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                  && event.getAction() == KeyEvent.ACTION_DOWN)) {
            lookup(barcode.getText().toString());
            return true;
          }
          return false;
        });
    ui.button("Look up product", () -> lookup(barcode.getText().toString()));
    ui.button(
        "Trigger configured scanner",
        () -> {
          if (scanner != null) scanner.trigger();
        });
    if (home.selected != null) {
      var p = home.selected;
      ui.text(p.productName, 22);
      ui.text(p.productCode + " â€¢ " + p.barcode, 14);
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
      ui.button(p.productCode + " Â· " + p.productName, () -> home.select(p));
  }

  public void onDestroyView() {
    ui = null;
    status = null;
    super.onDestroyView();
  }
}
