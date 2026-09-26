package com.company.pda.presentation.product;

import android.content.Context;
import android.view.*;
import androidx.appcompat.widget.AppCompatImageView;

public class ZoomImageView extends AppCompatImageView {

  private float zoom = 1, x, y, lastX, lastY;

  private final ScaleGestureDetector scale;

  public ZoomImageView(Context c) {

    super(c);

    setScaleType(ScaleType.FIT_CENTER);

    scale =
        new ScaleGestureDetector(
            c,
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {

              @Override
              public boolean onScale(ScaleGestureDetector d) {

                zoom = Math.max(1, Math.min(5, zoom * d.getScaleFactor()));

                invalidate();

                return true;
              }
            });
  }

  @Override
  protected void onDraw(android.graphics.Canvas canvas) {

    canvas.save();

    canvas.translate(x, y);

    canvas.scale(zoom, zoom, getWidth() / 2f, getHeight() / 2f);

    super.onDraw(canvas);

    canvas.restore();
  }

  @Override
  public boolean onTouchEvent(android.view.MotionEvent event) {

    scale.onTouchEvent(event);

    getParent().requestDisallowInterceptTouchEvent(event.getPointerCount() > 1 || zoom > 1);

    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

      lastX = event.getX();

      lastY = event.getY();

    } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE && !scale.isInProgress()) {

      x += event.getX() - lastX;

      y += event.getY() - lastY;

      lastX = event.getX();

      lastY = event.getY();

      float maxX = getWidth() * (zoom - 1) / 2, maxY = getHeight() * (zoom - 1) / 2;

      x = Math.max(-maxX, Math.min(maxX, x));

      y = Math.max(-maxY, Math.min(maxY, y));

      invalidate();
    }

    if (event.getActionMasked() == MotionEvent.ACTION_UP) performClick();

    return true;
  }

  @Override
  public boolean performClick() {

    super.performClick();

    return true;
  }
}
