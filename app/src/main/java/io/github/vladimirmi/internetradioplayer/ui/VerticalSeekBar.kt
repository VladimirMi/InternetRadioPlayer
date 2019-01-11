package io.github.vladimirmi.internetradioplayer.ui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView

/**
 * Created by Vladimir Mikhalev 11.01.2019.
 */

class VerticalSeekBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val bar = SeekBar(context)
    private val label = TextView(context)
    private var minProgress = 0

    init {
        bar.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(bar)
        addView(label)
    }

    fun setup(label: String, progress: Int, min: Int, max: Int) {
        this.label.text = label
        minProgress = min
        bar.max = max - min
        bar.progress = progress - min
    }

    fun setOnSeekBarChangeListener(l: SeekBar.OnSeekBarChangeListener?) {
        if (l == null) {
            bar.setOnSeekBarChangeListener(null)
            return
        }

        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(label, widthMeasureSpec, heightMeasureSpec)
        val barHeight = MeasureSpec.getSize(heightMeasureSpec) - label.measuredHeight
        measureChild(bar, MeasureSpec.makeMeasureSpec(barHeight, MeasureSpec.EXACTLY), widthMeasureSpec)

        val height = bar.measuredWidth + label.measuredHeight
        val width = Math.max(bar.measuredHeight, label.measuredWidth)

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        layoutSeekBar(l, t, r, b)
        layoutLabel(l, t, r, b)
    }

    private fun layoutSeekBar(l: Int, t: Int, r: Int, b: Int) {
        val h = bar.measuredHeight
        val w = bar.measuredWidth
        val containerWidth = r - l
        val left = (containerWidth - h) / 2

        bar.layout(left, w, left + w, w + h)

        bar.pivotX = 0f
        bar.pivotY = 0f
        bar.rotation = -90f
    }

    private fun layoutLabel(l: Int, t: Int, r: Int, b: Int) {
        val h = label.measuredHeight
        val w = label.measuredWidth
        val containerWidth = r - l
        val left = (containerWidth - w) / 2

        label.layout(left, b - h, left + w, b)
    }
}