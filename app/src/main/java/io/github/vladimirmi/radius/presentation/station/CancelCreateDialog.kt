package io.github.vladimirmi.radius.presentation.station

import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.ui.base.BaseDialogFragment

/**
 * Created by Vladimir Mikhalev 31.12.2017.
 */

class CancelCreateDialog : BaseDialogFragment() {

    override fun getTitle(): String {
        return getString(R.string.dialog_cancel_create_message)
    }

    override fun onPositive() {
        (parentFragment as StationFragment).presenter.cancelCreate()
    }

    override fun onNegative() {
    }
}