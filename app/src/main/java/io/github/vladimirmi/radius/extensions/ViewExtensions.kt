package io.github.vladimirmi.radius.extensions

import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewTreeObserver

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