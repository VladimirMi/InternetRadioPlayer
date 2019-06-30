package io.github.vladimirmi.internetradioplayer.ui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import io.github.vladimirmi.internetradioplayer.extensions.setProgressX

/**
 * Created by Vladimir Mikhalev 11.01.2019.
 */

class VerticalSeekBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val seekBar = SeekBar(context)
    private val label = TextView(context)
    private var minProgress = 0

    init {
        seekBar.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(seekBar)
        addView(label)
    }


    fun setup(label: String, min: Int, max: Int) {
        this.label.text = label
        minProgress = min
        seekBar.max = max - min
    }

    fun setProgress(progress: Int, animate: Boolean) {
        val actual = progress - minProgress
        seekBar.setProgressX(actual, animate)
    }

    fun getProgress(): Int {
        return seekBar.progress + minProgress
    }

    fun setOnSeekBarChangeListener(l: SeekBar.OnSeekBarChangeListener?) {
        if (l == null) {
            seekBar.setOnSeekBarChangeListener(null)
            return
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                l.onProgressChanged(seekBar, progress + minProgress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                l.onStartTrackingTouch(seekBar)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                l.onStopTrackingTouch(seekBar)
            }
        })
    }

    override fun setEnabled(enabled: Boolean) {
        seekBar.isEnabled = enabled
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(label, widthMeasureSpec, heightMeasureSpec)
        val barHeight = MeasureSpec.getSize(heightMeasureSpec) - label.measuredHeight
        measureChild(seekBar, MeasureSpec.makeMeasureSpec(barHeight, MeasureSpec.EXACTLY), widthMeasureSpec)

        val height = seekBar.measuredWidth + label.measuredHeight
        val width = Math.max(seekBar.measuredHeight, label.measuredWidth)

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        layoutSeekBar(l, t, r, b)
        layoutLabel(l, t, r, b)
    }

    private fun layoutSeekBar(l: Int, t: Int, r: Int, b: Int) {
        val h = seekBar.measuredHeight
        val w = seekBar.measuredWidth
        val containerWidth = r - l
        val left = (containerWidth - h) / 2

        seekBar.layout(left, w, left + w, w + h)

        seekBar.pivotX = 0f
        seekBar.pivotY = 0f
        seekBar.rotation = -90f
    }

    private fun layoutLabel(l: Int, t: Int, r: Int, b: Int) {
        val h = label.measuredHeight
        val w = label.measuredWidth
        val containerWidth = r - l
        val left = (containerWidth - w) / 2

        label.layout(left, b - h, left + w, b)
    }
}