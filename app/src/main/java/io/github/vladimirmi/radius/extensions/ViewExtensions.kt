package io.github.vladimirmi.radius.extensions

import android.support.annotation.ColorInt
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView

/**
 * Created by Vladimir Mikhalev 17.11.2017.
 */

inline fun View.waitForMeasure(crossinline block: () -> Unit) {
    if (width > 0 && height > 0) {
        block()
        return
    }
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            val observer = viewTreeObserver
            if (observer.isAlive) {
                observer.removeOnPreDrawListener(this)
            }
            block()
            return true
        }
    })
}

fun View.visible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun ImageView.setTint(@ColorInt colorInt: Int) {
    val wrapped = DrawableCompat.wrap(background).mutate()
    DrawableCompat.setTint(wrapped, colorInt)
}

fun View.setTint(@ColorInt colorInt: Int) {
    val wrapped = DrawableCompat.wrap(background).mutate()
    DrawableCompat.setTint(wrapped, colorInt)
}