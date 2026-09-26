package com.company.pda.common.util;

import android.content.Context;
import android.widget.*;

public class ScreenViews {
  public final LinearLayout content;
  private final Context context;

  public ScreenViews(LinearLayout content) {
    this.content = content;
    context = content.getContext();
  }

  public void clear() {
    content.removeAllViews();
  }

  public TextView text(String value, int size) {
    var view = new TextView(context);
    view.setText(value);
    view.setTextSize(size);
    view.setTextColor(0xff12352d);
    view.setPadding(0, 16, 0, 12);
    content.addView(view);
    return view;
  }

  public EditText input(String hint, String value, int type) {
    var view = new EditText(context);
    view.setSingleLine(true);
    view.setHint(hint);
    view.setText(value);
    view.setInputType(type);
    content.addView(view);
    return view;
  }

  public Button button(String title, Runnable action) {
    var view = new Button(context);
    view.setText(title);
    view.setAllCaps(false);
    view.setOnClickListener(v -> action.run());
    content.addView(view);
    return view;
  }

  public static LinearLayout column(Context context) {
    var view = new LinearLayout(context);
    view.setOrientation(LinearLayout.VERTICAL);
    view.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
    return view;
  }
}
