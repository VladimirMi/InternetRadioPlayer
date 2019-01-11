package io.github.vladimirmi.internetradioplayer.ui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

class EqualizerContainer @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    public fun setBands(bands: List<String>, values: List<Int>, min: Int, max: Int) {
        bands.forEachIndexed { index, band ->
            val seekBar = VerticalSeekBar(context)
            seekBar.layoutParams = LayoutParams(width / bands.size, ViewGroup.LayoutParams.MATCH_PARENT)
            seekBar.setup(band, values[index], min, max)
            addView(seekBar)
        }
    }
}