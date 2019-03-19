package io.github.vladimirmi.internetradioplayer.extensions

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce


/**
 * Created by Vladimir Mikhalev 17.11.2017.
 */

fun runOnUiThread(action: () -> Unit) {
    val mainLooper = Looper.getMainLooper()
    if (Thread.currentThread().id != mainLooper.thread.id) {
        Handler(mainLooper).post(action)
    } else {
        action.invoke()
    }
}

fun runOnUiThreadDelayed(delayMs: Long, action: () -> Unit) {
    val mainLooper = Looper.getMainLooper()
    Handler(mainLooper).postDelayed(action, delayMs)
}

inline fun View.waitForMeasure(crossinline block: () -> Unit) {
    if (width > 0 && height > 0) {
        block()
        return
    }
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            val observer = viewTreeObserver
            if (observer.isAlive) observer.removeOnPreDrawListener(this)
            block()
            return true
        }
    })
}

inline fun View.waitForLayout(crossinline handler: () -> Boolean) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val observer = viewTreeObserver
            if (handler.invoke() && observer.isAlive) {
                observer.removeOnGlobalLayoutListener(this)
            }
        }
    })
}

fun View.visible(visible: Boolean, gone: Boolean = true) {
    visibility = if (visible) View.VISIBLE else if (gone) View.GONE else View.INVISIBLE
}

val View?.isVisible: Boolean
    get() = this?.visibility == View.VISIBLE

fun TextView.onTextChanges(listener: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            listener.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun SeekBar.setProgressX(progress: Int, animate: Boolean) {
    if (animate) {
        if (Build.VERSION.SDK_INT >= 24) {
            setProgress(progress, animate)
        } else {
            with(ObjectAnimator.ofInt(this, "progress", progress)) {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    } else {
        setProgress(progress)
    }
}

fun View.bounceXAnimation(dpVelocity: Float): SpringAnimation {
    return SpringAnimation(this, DynamicAnimation.TRANSLATION_X, 0f)
            .setStartVelocity(dpVelocity * context.dp).apply {
                spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                spring.stiffness = 1000f
            }
}

fun DrawerLayout.lock(locked: Boolean) {
    setDrawerLockMode(if (locked) DrawerLayout.LOCK_MODE_LOCKED_CLOSED
    else DrawerLayout.LOCK_MODE_UNLOCKED)
}
