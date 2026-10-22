package com.company.pda.common.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.company.pda.R;

/** An activity-owned overlay: only the bubble consumes touches, never the rest of the screen. */
final class SessionBubble {
  private final FrameLayout overlay;
  private final TextView bubble;
  private final SharedPreferences position;
  private final int margin;
  private final int touchSlop;
  private boolean right;
  private float vertical;
  private float downX, downY, startX, startY;
  private boolean dragging;

  @SuppressLint("ClickableViewAccessibility")
  SessionBubble(Activity activity, Runnable onClick) {
    position = activity.getSharedPreferences("session_bubble", Context.MODE_PRIVATE);
    right = position.getBoolean("right", true);
    vertical = position.getFloat("vertical", 0.2f);
    margin = Math.round(12 * activity.getResources().getDisplayMetrics().density);
    touchSlop = ViewConfiguration.get(activity).getScaledTouchSlop();
    overlay = new FrameLayout(activity);
    overlay.setClipChildren(false);
    bubble = new TextView(activity);
    bubble.setId(R.id.session_bubble);
    bubble.setGravity(Gravity.CENTER);
    bubble.setTextSize(14);
    bubble.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
    bubble.setTextColor(Color.WHITE);
    bubble.setPadding(margin, margin / 2, margin, margin / 2);
    bubble.setMinHeight(margin * 5);
    bubble.setElevation(margin / 2f);
    bubble.setClickable(true);
    bubble.setFocusable(true);
    bubble.setOnClickListener(view -> onClick.run());
    overlay.addView(
        bubble,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    ViewGroup content = activity.findViewById(android.R.id.content);
    content.addView(
        overlay,
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    overlay.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> place());
    bubble.addOnLayoutChangeListener(
        (v, l, t, r, b, ol, ot, or, ob) -> {
          if (r - l != or - ol || b - t != ob - ot) place();
        });
    ViewCompat.setOnApplyWindowInsetsListener(
        overlay,
        (view, insets) -> {
          // Padding also keeps the bubble above an open keyboard.
          var safe =
              insets.getInsets(
                  WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
          overlay.setPadding(safe.left, safe.top, safe.right, safe.bottom);
          overlay.post(this::place);
          return insets;
        });
    ViewCompat.requestApplyInsets(overlay);
    bubble.setOnTouchListener(
        (view, event) -> {
          switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
              downX = event.getRawX();
              downY = event.getRawY();
              startX = bubble.getX();
              startY = bubble.getY();
              dragging = false;
              return true;
            case MotionEvent.ACTION_MOVE:
              float dx = event.getRawX() - downX;
              float dy = event.getRawY() - downY;
              if (Math.hypot(dx, dy) > touchSlop) dragging = true;
              if (dragging) {
                bubble.setX(clamp(startX + dx, minX(), maxX()));
                bubble.setY(clamp(startY + dy, minY(), maxY()));
              }
              return true;
            case MotionEvent.ACTION_UP:
              if (dragging) {
                right = bubble.getX() + bubble.getWidth() / 2f >= overlay.getWidth() / 2f;
                vertical =
                    maxY() > minY() ? (bubble.getY() - minY()) / (maxY() - minY()) : vertical;
                position.edit().putBoolean("right", right).putFloat("vertical", vertical).apply();
                dragging = false;
                place();
              } else view.performClick();
              return true;
            case MotionEvent.ACTION_CANCEL:
              dragging = false;
              place();
              return true;
            default:
              return false;
          }
        });
  }

  void update(String remaining, boolean warning, boolean renewing) {
    String label = renewing ? "Extending..." : warning ? "Expiring" : "Session";
    String text = label + "\n" + remaining;
    if (!text.contentEquals(bubble.getText())) bubble.setText(text);
    bubble.setContentDescription(
        "Session time remaining: "
            + remaining
            + ". Tap for session details and extension. Drag to move.");
    GradientDrawable background = new GradientDrawable();
    background.setColor(Color.parseColor(warning ? "#A33A16" : "#124F46"));
    background.setCornerRadius(margin * 3f);
    bubble.setBackground(background);
  }

  void detach() {
    ViewGroup parent = (ViewGroup) overlay.getParent();
    if (parent != null) parent.removeView(overlay);
  }

  private void place() {
    if (dragging || overlay.getWidth() == 0 || bubble.getWidth() == 0) return;
    bubble.setX(right ? maxX() : minX());
    bubble.setY(minY() + clamp(vertical, 0, 1) * (maxY() - minY()));
  }

  private float minX() {
    return overlay.getPaddingLeft() + margin;
  }

  private float minY() {
    return overlay.getPaddingTop() + margin;
  }

  private float maxX() {
    return Math.max(
        minX(), overlay.getWidth() - overlay.getPaddingRight() - bubble.getWidth() - margin);
  }

  private float maxY() {
    return Math.max(
        minY(), overlay.getHeight() - overlay.getPaddingBottom() - bubble.getHeight() - margin);
  }

  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(value, max));
  }
}
