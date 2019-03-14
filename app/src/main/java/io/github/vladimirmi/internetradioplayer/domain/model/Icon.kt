package io.github.vladimirmi.internetradioplayer.domain.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.getRandomDarkColor
import io.github.vladimirmi.internetradioplayer.extensions.getRandomLightColor
import io.github.vladimirmi.internetradioplayer.extensions.setTintExt
import java.util.*

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

data class Icon(val res: Int,
                val bg: Int,
                val fg: Int) {

    companion object {
        fun randomIcon(seed: Long): Icon {
            val random = Random(seed)
            return Icon(res = random.nextInt(ICONS.size),
                    fg = getRandomDarkColor(random),
                    bg = getRandomLightColor(random))
        }
    }

    fun getBitmap(context: Context, withBackground: Boolean = false): Bitmap {
        val iconSize = 128
        val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (withBackground) {
            with(ContextCompat.getDrawable(context, R.drawable.bg_icon)!!) {
                setTintExt(bg)
                setBounds(0, 0, canvas.width, canvas.height)
                draw(canvas)
            }
        }
        with(ContextCompat.getDrawable(context, ICONS[res])!!) {
            setTintExt(fg)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
        return bitmap
    }
}

val ICONS = arrayOf(
        R.drawable.ic_station_1,
        R.drawable.ic_station_2,
        R.drawable.ic_station_3,
        R.drawable.ic_station_4,
        R.drawable.ic_station_5
)
