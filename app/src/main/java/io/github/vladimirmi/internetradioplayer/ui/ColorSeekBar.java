package io.github.vladimirmi.internetradioplayer.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.graphics.drawable.DrawableCompat;
import io.github.vladimirmi.internetradioplayer.R;

/**
 * Created by Vladimir Mikhalev 16.12.2017.
 */

public class ColorSeekBar extends AppCompatSeekBar {

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
