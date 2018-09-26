package io.github.vladimirmi.internetradioplayer.ui

import android.graphics.Outline
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.View
import android.view.ViewOutlineProvider
import io.github.vladimirmi.internetradioplayer.R

/**
 * Created by Vladimir Mikhalev 26.09.2018.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class FixedOutlineProvider : ViewOutlineProvider() {

    override fun getOutline(view: View, outline: Outline) {
        outline.setRect(
                0,
                view.resources.getDimensionPixelSize(R.dimen.item_card_elevation),
                view.width,
                view.height
        )
    }
}
