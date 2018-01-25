package io.github.vladimirmi.internetradioplayer.model.entity

import android.graphics.Bitmap
import android.graphics.Color
import io.github.vladimirmi.internetradioplayer.presentation.iconpicker.IconOption
import io.github.vladimirmi.internetradioplayer.presentation.iconpicker.IconRes

/**
 * Created by Vladimir Mikhalev 24.12.2017.
 */

data class Icon(val name: String,
                val bitmap: Bitmap,
                val option: IconOption = IconOption.ICON,
                val iconRes: IconRes = IconRes.ICON_1,
                val backgroundColor: Int = Color.TRANSPARENT,
                val foregroundColor: Int = Color.BLACK,
                val text: String = "") {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Icon

        if (name != other.name) return false
        if (option != other.option) return false
        if (iconRes != other.iconRes) return false
        if (backgroundColor != other.backgroundColor) return false
        if (foregroundColor != other.foregroundColor) return false
        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + option.hashCode()
        result = 31 * result + iconRes.hashCode()
        result = 31 * result + backgroundColor
        result = 31 * result + foregroundColor
        result = 31 * result + text.hashCode()
        return result
    }
}