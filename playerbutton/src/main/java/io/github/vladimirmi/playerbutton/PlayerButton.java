package io.github.vladimirmi.playerbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
        setImageDrawable(getVectorDrawable());
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
     * Set playing mode. Animate by default
     *
     * @param play {@code true} - playing mode, {@code false} - paused mode
     */
    public void setPlaying(boolean play) {
        setPlaying(play, true);
    }

    /**
     * Set playing mode
     *
     * @param play    {@code true} - playing mode, {@code false} - paused mode
     * @param animate animate changes
     */
    public void setPlaying(boolean play, boolean animate) {
        if (isPlaying == play) return;
        isPlaying = play;
        if (animate) {
            setImageDrawable(getAnimatedDrawable());
            ((Animatable) getDrawable()).start();
        } else {
            setImageDrawable(getVectorDrawable());
        }
    }

    /**
     * Set manual mode
     * <p>In manual mode, you must to call {@link #setPlaying(boolean)} to change the button state.
     * Otherwise, the button will also change the state itself on click.</p>
     */
    public void setManualMode(boolean isManualMode) {
        this.isManualMode = isManualMode;
    }


    private AnimatedVectorDrawableCompat getAnimatedDrawable() {
        int resId = isPlaying ? R.drawable.play_to_pause_animation : R.drawable.pause_to_play_animation;
        AnimatedVectorDrawableCompat drawable = AnimatedVectorDrawableCompat.create(getContext(), resId);
        //noinspection ConstantConditions
        drawable.setTint(isPlaying ? pauseColor : playColor);
        return drawable;
    }

    private Drawable getVectorDrawable() {
        int resId = isPlaying ? R.drawable.icon_pause : R.drawable.icon_play;
        Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
        setTint(drawable, isPlaying ? pauseColor : playColor);
        return drawable;
    }

    private void setTint(Drawable drawable, @ColorInt int tint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.setTint(tint);
        } else {
            drawable.setColorFilter(tint, PorterDuff.Mode.SRC_IN);
        }
    }
}
