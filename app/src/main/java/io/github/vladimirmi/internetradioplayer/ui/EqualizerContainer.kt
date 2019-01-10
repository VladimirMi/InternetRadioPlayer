package io.github.vladimirmi.internetradioplayer.ui

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import io.github.vladimirmi.internetradioplayer.extensions.getScreenSize

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

class EqualizerContainer(context: Context?) : LinearLayout(context) {

    init {
        orientation = VERTICAL
    }

    public fun setBands(bands: List<String>, values: List<Int>, min: Int, max: Int) {
        val (x, y) = context.getScreenSize()

        val sliders = LinearLayout(context)

        for (value in values) {
            val seekBar = SeekBar(context).apply {
                //                setRange(min, max)
                setMax(max)
                progress = value
                layoutParams = ViewGroup.LayoutParams(600, x / (values.size))
            }
            val lable = TextView(context).apply {
                text = value.toString()
                gravity = Gravity.CENTER
            }

            val container = VerticalSeekBarContainer(context)
            container.addView(seekBar)
            container.addView(lable)
            sliders.addView(container)
        }

        addView(sliders)
    }
}