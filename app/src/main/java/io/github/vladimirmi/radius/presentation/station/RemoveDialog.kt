package io.github.vladimirmi.radius.presentation.station

import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.ui.base.BaseDialogFragment

/**
 * Created by Vladimir Mikhalev 28.12.2017.
 */

open class RemoveDialog : BaseDialogFragment() {

    override fun getTitle(): String {
        return getString(R.string.dialog_remove_message)
    }

    override fun onPositive() {
        (parentFragment as StationFragment).presenter.removeStation()
    }

    override fun onNegative() {
    }
}