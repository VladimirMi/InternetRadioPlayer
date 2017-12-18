package io.github.vladimirmi.radius.presentation.iconpicker

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

interface IconPickerView : MvpView {

    fun setIconImage(icon: Bitmap)

    fun setIconText(text: String)

    fun setIconTextColor(colorInt: Int)

    fun setBackgroundColor(colorInt: Int)

    fun hideStationUrlOption()

    fun hideTextOption()

    fun option(url: Boolean, name: Boolean, add: Boolean)
}