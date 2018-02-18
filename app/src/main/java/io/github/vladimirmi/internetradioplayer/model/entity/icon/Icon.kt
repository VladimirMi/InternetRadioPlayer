package io.github.vladimirmi.internetradioplayer.model.entity.icon

import android.graphics.Bitmap
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.getBitmap
import java.util.*

/**
 * Created by Vladimir Mikhalev 24.12.2017.
 */

sealed class Icon(var name: String,
                  val bitmap: Bitmap,
                  val option: IconOption) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Icon
        if (name != other.name) return false
        if (option != other.option) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + option.hashCode()
        return result
    }
}

class IconRes(name: String,
              val foregroundColor: Int = randomColor(),
              val res: IconResource = IconResource.random(),
              bitmap: Bitmap? = null)
    : Icon(name, bitmap ?: createBitmap(res, foregroundColor), IconOption.ICON) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        other as IconRes
        if (foregroundColor != other.foregroundColor) return false
        if (res != other.res) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + foregroundColor
        result = 31 * result + res.hashCode()
        return result
    }
}

@Suppress("MagicNumber")
fun randomColor(): Int {
    val random = Random()
    val r = random.nextInt(256)
    val g = random.nextInt(256)
    val b = random.nextInt(256)
    if ((r + g + b) > 460) return randomColor()
    return Color.rgb(r, g, b)
}

fun createBitmap(res: IconResource, color: Int): Bitmap {
    val drawable = DrawableCompat.wrap(ContextCompat.getDrawable(Scopes.context, res.resId)).mutate()
    DrawableCompat.setTint(drawable, color)
    return drawable.getBitmap()
}
