package io.github.vladimirmi.internetradioplayer.extensions

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
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


fun TextView.onTextChanges(listener: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            listener.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun View.bounceXAnimation(dpVelocity: Float): SpringAnimation {
    return SpringAnimation(this, DynamicAnimation.TRANSLATION_X, 0f)
            .setStartVelocity(dpVelocity * context.dp).apply {
                spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                spring.stiffness = 1000f
            }
}
