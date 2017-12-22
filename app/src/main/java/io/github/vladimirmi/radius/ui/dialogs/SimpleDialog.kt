package io.github.vladimirmi.radius.ui.dialogs

import android.content.DialogInterface
import android.view.ViewGroup
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.ui.base.BaseDialog
import kotlinx.android.synthetic.main.dialog_simple.view.*
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 30.11.2017.
 */

class SimpleDialog(viewGroup: ViewGroup) : BaseDialog(R.layout.dialog_simple, viewGroup) {

    fun onPositive(action: () -> Unit): SimpleDialog {
        setPositiveAction(DialogInterface.OnClickListener { _, _ ->
            action.invoke()
            Timber.e("onPositive")
        })
        return this
    }

    fun onNegative(action: () -> Unit): SimpleDialog {
        setNegativeAction({
            action.invoke()
            Timber.e("onNegative")
        })
        return this
    }

    fun setMessage(string: String): SimpleDialog {
        dialogView.dialog_message.text = string
        return this
    }
}