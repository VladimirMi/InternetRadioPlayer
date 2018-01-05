package io.github.vladimirmi.radius.model.entity

import android.graphics.Bitmap
import android.graphics.Color
import io.github.vladimirmi.radius.presentation.iconpicker.IconOption
import io.github.vladimirmi.radius.presentation.iconpicker.IconRes

/**
 * Created by Vladimir Mikhalev 24.12.2017.
 */

data class Icon(val name: String,
                val bitmap: Bitmap,
                val option: IconOption = IconOption.ICON,
                val iconRes: IconRes = IconRes.ICON_1,
                val backgroundColor: Int = Color.TRANSPARENT,
                val foregroundColor: Int = Color.BLACK,
                val text: String = "")