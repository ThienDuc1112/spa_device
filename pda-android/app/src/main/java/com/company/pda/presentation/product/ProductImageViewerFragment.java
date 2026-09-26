package com.company.pda.presentation.product;

import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.company.pda.R;
import com.company.pda.common.util.ScreenViews;

public class ProductImageViewerFragment extends Fragment {
  public static ProductImageViewerFragment forImage(String url) {
    var f = new ProductImageViewerFragment();
    var args = new Bundle();
    args.putString("imageUrl", url);
    f.setArguments(args);
    return f;
  }

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
    var box = ScreenViews.column(requireContext());
    var ui = new ScreenViews(box);
    ui.text("Pinch to zoom", 16);
    var image = new ZoomImageView(requireContext());
    image.setContentDescription("Product image; pinch to zoom");
    box.addView(image, new LinearLayout.LayoutParams(-1, 900));
    Glide.with(this)
        .load(getArguments() == null ? null : getArguments().getString("imageUrl"))
        .placeholder(R.drawable.product_placeholder)
        .error(R.drawable.product_placeholder)
        .fallback(R.drawable.product_placeholder)
        .into(image);
    return box;
  }
}
