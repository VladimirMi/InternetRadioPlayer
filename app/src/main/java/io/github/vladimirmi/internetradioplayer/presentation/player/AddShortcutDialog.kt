package io.github.vladimirmi.internetradioplayer.presentation.player

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseDialogFragment

/**
 * Created by Vladimir Mikhalev 18.09.2018.
 */

class AddShortcutDialog : BaseDialogFragment() {

    override fun getTitle(): String {
        return getString(R.string.menu_station_shortcut)
    }

    @SuppressLint("InflateParams")
    override fun getCustomDialogView(): View? {
        return LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_shortcut, null)
    }

    override fun onPositive() {
        dialogView?.let {
            //todo implement
//            (parentFragment as PlayerFragment).presenter.addShortcut(it.checkbox.isChecked)
        }
    }

    override fun onNegative() {
    }
}
