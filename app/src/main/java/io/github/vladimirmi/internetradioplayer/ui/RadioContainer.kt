package io.github.vladimirmi.internetradioplayer.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.RadioGroup

/**
 * Created by Vladimir Mikhalev 05.01.2018.
 */

class RadioContainer : FrameLayout {

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
        setOnClickListener {
            (parent as RadioGroup).check(getChildAt(0).id)
        }
    }
}