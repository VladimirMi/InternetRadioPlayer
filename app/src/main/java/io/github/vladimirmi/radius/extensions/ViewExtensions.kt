package io.github.vladimirmi.radius.extensions

import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView

/**
 * Created by Vladimir Mikhalev 17.11.2017.
 */

fun View.setBackgroundColorExt(id: Int) {
    setBackgroundColor(ContextCompat.getColor(context, id))
}

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

fun View.remove() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun ImageView.setTint(@ColorRes tint: Int) {
    val wrapped = DrawableCompat.wrap(drawable).mutate()
    DrawableCompat.setTint(wrapped, ContextCompat.getColor(context, tint))
}

fun View.setTint(@ColorRes tint: Int) {
    val wrapped = DrawableCompat.wrap(background).mutate()
    DrawableCompat.setTint(wrapped, ContextCompat.getColor(context, tint))
}