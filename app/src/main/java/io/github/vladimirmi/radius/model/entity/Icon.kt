package io.github.vladimirmi.radius.model.entity

import android.graphics.Bitmap
import android.graphics.Color
import io.github.vladimirmi.radius.presentation.iconpicker.IconOption

/**
 * Created by Vladimir Mikhalev 24.12.2017.
 */

data class Icon(val name: String,
                val bitmap: Bitmap,
                val backGroundColor: Int = Color.LTGRAY,
                val textColor: Int = Color.BLACK,
                val text: String = "",
                val option: IconOption = IconOption.DEFAULT)