package io.github.vladimirmi.playerbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;


public class PlayerButton extends androidx.appcompat.widget.AppCompatImageButton {

    private final @ColorInt int playColor;
    private final @ColorInt int pauseColor;
    private boolean isPlaying = false;
    private boolean isManualMode = false;
    private OnClickListener listener;

    public PlayerButton(Context context) {
        this(context, null);
    }

    public PlayerButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PlayerButton,
                0, 0);

        try {
            int defaultColor = ContextCompat.getColor(context, R.color.default_icon_color);
            playColor = a.getColor(R.styleable.PlayerButton_pb_playColor, defaultColor);
            pauseColor = a.getColor(R.styleable.PlayerButton_pb_pauseColor, defaultColor);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        setDrawable(true);
        super.setOnClickListener(v -> {
            if (listener != null) listener.onClick(v);
            if (!isManualMode) setPlaying(!isPlaying);
        });
    }

    @Override
    public void setOnClickListener(@Nullable final OnClickListener l) {
        listener = l;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Set playing mode. Animate if needed
     *
     * @param isPlaying {@code true} - playing mode, {@code false} - paused mode
     */
    public void setPlaying(boolean isPlaying) {
        if (this.isPlaying != isPlaying) {
            setDrawable(false);
            ((Animatable) getDrawable()).start();
        }
        this.isPlaying = isPlaying;
    }

    /**
     * Set manual mode
     * <p>In manual mode, you must to call {@link #setPlaying(boolean)} to change the button state.
     * Otherwise, the button will also change the state itself on click.</p>
     */
    public void setManualMode(boolean isManualMode) {
        this.isManualMode = isManualMode;
    }

    private void setDrawable(boolean init) {
        AnimatedVectorDrawableCompat drawable = getNextDrawable();
        if (drawable != null) {
            tintDrawable(drawable, isPlaying || init ? playColor : pauseColor);
            setImageDrawable(drawable);
        }
    }

    private AnimatedVectorDrawableCompat getNextDrawable() {
        int resId = isPlaying ? R.drawable.pause_to_play_animation : R.drawable.play_to_pause_animation;
        return AnimatedVectorDrawableCompat.create(getContext(), resId);
    }

    private void tintDrawable(@NonNull Drawable drawable, @ColorInt int color) {
        Drawable wrapped = DrawableCompat.wrap(drawable).mutate();
        DrawableCompat.setTint(wrapped, color);
    }
}
