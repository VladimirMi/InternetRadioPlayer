package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.ui.base.BaseDialogFragment

/**
 * Created by Vladimir Mikhalev 28.12.2017.
 */

open class RemoveDialog : BaseDialogFragment() {

    override fun getTitle(): String {
        return getString(R.string.dialog_remove_message)
    }

    override fun onPositive() {
        (parentFragment as StationListFragment).presenter.removeStation()
    }

    override fun onNegative() {
    }
}
