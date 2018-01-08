package io.github.vladimirmi.radius.extensions

import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView

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
    background?.setTintExt(colorInt)
}

fun View.setTint(@ColorInt colorInt: Int) {
    background?.setTintExt(colorInt)
}

fun Drawable.setTintExt(@ColorInt colorInt: Int) {
    val wrapped = DrawableCompat.wrap(this)
    DrawableCompat.setTint(wrapped, colorInt)
}

fun TextView.onTextChanges(listener: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            listener.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}