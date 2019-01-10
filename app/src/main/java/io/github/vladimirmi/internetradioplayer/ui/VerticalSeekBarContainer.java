package io.github.vladimirmi.internetradioplayer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class VerticalSeekBarContainer extends ViewGroup {

    public VerticalSeekBarContainer(Context context) {
        super(context);
    }

    public VerticalSeekBarContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalSeekBarContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;
        int width = 0;

        final View bar = getChildAt(0);
        if (bar.getVisibility() != GONE) {
            measureChild(bar, widthMeasureSpec, heightMeasureSpec);
            height = bar.getMeasuredWidth();
            width = bar.getMeasuredHeight();

            setMeasuredDimension(
                    bar.getMeasuredHeight(),
                    bar.getMeasuredWidth());
        }

        final View label = getChildAt(1);
        if (label.getVisibility() != GONE) {
            measureChild(label, widthMeasureSpec, heightMeasureSpec);

            height += label.getMeasuredHeight() - 4 * 3;
            width = Math.max(width, label.getMeasuredWidth());
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final View bar = getChildAt(0);
        if (bar.getVisibility() != GONE) {
            int h = bar.getMeasuredHeight();
            int w = bar.getMeasuredWidth();
            bar.layout(0, w, w, w + h);

            bar.setPivotX(0);
            bar.setPivotY(0);
            bar.setRotation(-90);
        }

        final View label = getChildAt(1);
        if (label.getVisibility() != GONE) {
            int h = label.getMeasuredHeight();
            int w = label.getMeasuredWidth();
            int containerWidth = r - l;
            int left = (containerWidth - w) / 2;
            int right = left + w;
            label.layout(left, b - h, right, b);
        }
    }
}
