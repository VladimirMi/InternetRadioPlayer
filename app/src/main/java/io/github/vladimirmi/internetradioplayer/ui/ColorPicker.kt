package io.github.vladimirmi.internetradioplayer.ui

import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.annotation.IntRange
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import io.github.vladimirmi.internetradioplayer.R
import kotlinx.android.synthetic.main.view_color_picker.view.*

/**
 * Created by Vladimir Mikhalev 16.12.2017.
 */

class ColorPicker : FrameLayout, SeekBar.OnSeekBarChangeListener {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val view = View.inflate(context, R.layout.view_color_picker, null)
        addView(view)
        view.redBar.setOnSeekBarChangeListener(this)
        view.greenBar.setOnSeekBarChangeListener(this)
        view.blueBar.setOnSeekBarChangeListener(this)
    }

    @ColorInt private var color: Int = Color.LTGRAY
    private var listener: ((Int) -> Unit)? = null

    fun setOnColorChangedListener(listener: (Int) -> Unit) {
        this.listener = listener
    }

    fun setColor(@ColorInt colorInt: Int) {
        Color.red(colorInt).let {
            redBar.progress = it
            redValue.text = it.toString()
        }
        Color.green(colorInt).let {
            greenBar.progress = it
            greenValue.text = it.toString()
        }
        Color.blue(colorInt).let {
            blueBar.progress = it
            blueValue.text = it.toString()
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        when (seekBar.id) {
            R.id.redBar -> if (fromUser) setRed(progress)
            R.id.greenBar -> if (fromUser) setGreen(progress)
            R.id.blueBar -> if (fromUser) setBlue(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    private fun setRed(@IntRange(from = 0, to = 255) int: Int) {
        redValue.text = int.toString()
        updateColor()
    }

    private fun setGreen(@IntRange(from = 0, to = 255) int: Int) {
        greenValue.text = int.toString()
        updateColor()
    }

    private fun setBlue(@IntRange(from = 0, to = 255) int: Int) {
        blueValue.text = int.toString()
        updateColor()
    }

    private fun updateColor() {
        color = Color.rgb(redBar.progress, greenBar.progress, blueBar.progress)
        listener?.invoke(color)
    }
}
