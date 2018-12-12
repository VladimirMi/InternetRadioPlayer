package io.github.vladimirmi.internetradioplayer.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.tabs.TabLayout
import io.github.vladimirmi.internetradioplayer.extensions.getScreenSize
import java.lang.reflect.Field


/**
 * Created by Vladimir Mikhalev 12.12.2018.
 */

private const val NUM_PAGES = 4

class ScrollableTabLayout : TabLayout {

    constructor(context: Context) : super(context) {
        initTabMinWidth()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initTabMinWidth()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initTabMinWidth()
    }

    private fun initTabMinWidth() {
        val (width, height) = context.getScreenSize()
        val tabMinWidth = width / NUM_PAGES

        val field: Field
        try {
            field = TabLayout::class.java.getDeclaredField("scrollableTabMinWidth")
            field.isAccessible = true
            field.set(this, tabMinWidth)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}