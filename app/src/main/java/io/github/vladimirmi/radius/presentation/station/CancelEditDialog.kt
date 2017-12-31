package io.github.vladimirmi.radius.presentation.station

import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.ui.base.BaseDialogFragment

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