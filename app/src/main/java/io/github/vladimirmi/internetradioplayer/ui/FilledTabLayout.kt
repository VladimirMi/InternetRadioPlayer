package io.github.vladimirmi.internetradioplayer.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.tabs.TabLayout
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.getScreenSize
import java.lang.reflect.Field


/**
 * Created by Vladimir Mikhalev 12.12.2018.
 */

class FilledTabLayout : TabLayout {

    constructor(context: Context) : super(context) {
        initTabMinWidth(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initTabMinWidth(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initTabMinWidth(attrs)
    }

    private fun initTabMinWidth(attrs: AttributeSet?) {
        if (attrs == null) return
        val array = context.obtainStyledAttributes(attrs, R.styleable.FilledTabLayout)
        val tabsNumber = array.getInteger(R.styleable.FilledTabLayout_tabsNumber, 0)
        array.recycle()
        if (tabsNumber == 0) return

        val (width, _) = context.getScreenSize()
        val tabMinWidth = width / tabsNumber

        val minWidthField: Field
        val maxWidthField: Field
        try {
            minWidthField = TabLayout::class.java.getDeclaredField("requestedTabMinWidth")
            maxWidthField = TabLayout::class.java.getDeclaredField("requestedTabMaxWidth")
            minWidthField.isAccessible = true
            maxWidthField.isAccessible = true
            minWidthField.set(this, tabMinWidth)
            maxWidthField.set(this, 0)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}