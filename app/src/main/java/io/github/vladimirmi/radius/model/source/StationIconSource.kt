package io.github.vladimirmi.radius.model.source

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.extensions.dp
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 08.12.2017.
 */

class StationIconSource
@Inject constructor(private val context: Context) {

    private val sizeDp = 48
    private val size = sizeDp * context.dp

    /**
     * Returns pair of text and background colors accordingly
     *
     * @param stationTitle Station title for which icon colors return
     * @return Pair of colors in the form 0xAARRGGBB
     */
    fun getIconTextColors(stationTitle: String): Pair<Int, Int> {
        val textColors = context.resources.getIntArray(R.array.icon_text_color_set)
        val bgColors = context.resources.getIntArray(R.array.icon_text_color_set)
        val colorIdx = stationTitle.first().toInt() % textColors.size
        return Pair(textColors[colorIdx], bgColors[colorIdx])
    }

    fun getIconView(stationTitle: String,
                    colors: Pair<Int, Int> = getIconTextColors(stationTitle)): TextView {
        with(TextView(context)) {
            gravity = Gravity.CENTER
            text = stationTitle.substring(0, 1)
            setTextColor(colors.first)
            typeface = ResourcesCompat.getFont(context, R.font.audiowide)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, sizeDp * 3f / 4 / text.length)

            val spec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
            measure(spec, spec)
            layout(0, 0, size, size)

            val bg = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.bg_rounded))
            DrawableCompat.setTint(bg, colors.second)
            background = bg
            return this
        }
    }

    fun getBitmap(stationTitle: String,
                  colors: Pair<Int, Int> = getIconTextColors(stationTitle)): Bitmap {
        val b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        getIconView(stationTitle, colors).draw(Canvas(b))
        return b
    }


}