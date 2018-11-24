package io.github.vladimirmi.internetradioplayer.presentation.player

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseDialogFragment

/**
 * Created by Vladimir Mikhalev 31.12.2017.
 */

class CancelCreateDialog : BaseDialogFragment() {

    override fun getTitle(): String {
        return getString(R.string.dialog_cancel_create_message)
    }

    override fun onPositive() {
        (parentFragment as PlayerFragment).presenter.cancelCreate()
    }

    override fun onNegative() {
    }
}
