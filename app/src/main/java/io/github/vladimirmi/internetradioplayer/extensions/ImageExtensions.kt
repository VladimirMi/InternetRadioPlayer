package io.github.vladimirmi.internetradioplayer.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.support.annotation.ColorInt
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.ICONS
import io.github.vladimirmi.internetradioplayer.data.db.entity.Icon
import java.util.*


/**
 * Created by Vladimir Mikhalev 08.09.2018.
 */

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

fun Drawable.setTintExt(@ColorInt tint: Int) {
    if (Build.VERSION.SDK_INT >= 21) {
        mutate().setTint(tint)
    } else {
        mutate().setColorFilter(tint, PorterDuff.Mode.SRC_IN)
    }
}

fun Icon.getBitmap(context: Context, withBackground: Boolean = false): Bitmap {
    val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    if (withBackground) {
        val background = ContextCompat.getDrawable(context, R.drawable.ic_background)!!
        background.setTintExt(bg)
        background.setBounds(0, 0, canvas.width, canvas.height)
        background.draw(canvas)
    }

    val drawable = ContextCompat.getDrawable(context, ICONS[res])!!
    drawable.setTintExt(fg)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun getRandomDarkColor(random: Random): Int {
    val darkThreshold = 0.4

    val color = getRandomColor(random)
    val luminance = ColorUtils.calculateLuminance(color)

    return if (luminance > darkThreshold) getRandomDarkColor(random) else color
}

fun getRandomLightColor(random: Random): Int {
    val lightThreshold = 0.6

    val color = getRandomColor(random)
    val luminance = ColorUtils.calculateLuminance(color)

    return if (luminance < lightThreshold) getRandomLightColor(random) else color
}

fun getRandomColor(random: Random): Int {
    val r = random.nextInt(256)
    val g = random.nextInt(256)
    val b = random.nextInt(256)
    return Color.rgb(r, g, b)
}
