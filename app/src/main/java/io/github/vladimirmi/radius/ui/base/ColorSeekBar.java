package io.github.vladimirmi.radius.ui.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;

import io.github.vladimirmi.radius.R;

/**
 * Created by Vladimir Mikhalev 16.12.2017.
 */

public class ColorSeekBar extends android.support.v7.widget.AppCompatSeekBar {

    public ColorSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorSeekBar);
        int color = a.getColor(R.styleable.ColorSeekBar_color, 0xff000000);
        a.recycle();

        Drawable progress = DrawableCompat.wrap(getProgressDrawable()).mutate();
        Drawable thumb = DrawableCompat.wrap(getThumb()).mutate();
        DrawableCompat.setTint(progress, color);
        DrawableCompat.setTint(thumb, color);
    }
}
