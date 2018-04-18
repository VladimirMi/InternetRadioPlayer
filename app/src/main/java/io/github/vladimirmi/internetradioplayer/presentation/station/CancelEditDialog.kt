package io.github.vladimirmi.internetradioplayer.presentation.station

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.ui.base.BaseDialogFragment

/**
 * Created by Vladimir Mikhalev 31.12.2017.
 */

class CancelEditDialog : BaseDialogFragment() {

    override fun getTitle(): String {
        return getString(R.string.dialog_cancel_edit_message)
    }

    override fun onPositive() {
        (parentFragment as StationFragment).presenter.cancelEdit()
    }

    override fun onNegative() {
    }
}
