package io.github.vladimirmi.internetradioplayer.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.widget.ImageView

/**
 * Created by Vladimir Mikhalev 08.09.2018.
 */

fun Context.getBitmap(@DrawableRes id: Int): Bitmap {
    return ContextCompat.getDrawable(this, id)!!.getBitmap()
}

@SuppressLint("NewApi")
fun Drawable.getBitmap(): Bitmap {
    return when (this) {
        is BitmapDrawable -> this.bitmap

        is VectorDrawableCompat, is VectorDrawable -> {
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)

            bitmap
        }
        else -> {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }
}

fun View.setBgTint(@ColorInt colorInt: Int) {
    background?.setTintExt(colorInt)
}

fun ImageView.setFgTint(@ColorInt colorInt: Int) {
    drawable?.setTintExt(colorInt)
}

fun Drawable.setTintExt(@ColorInt colorInt: Int) {
    val wrapped = DrawableCompat.wrap(this)
    DrawableCompat.setTint(wrapped, colorInt)
}
