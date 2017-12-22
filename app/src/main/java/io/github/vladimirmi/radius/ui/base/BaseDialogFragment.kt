package io.github.vladimirmi.radius.ui.base

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import io.github.vladimirmi.radius.R

/**
 * Created by Vladimir Mikhalev 22.12.2017.
 */


abstract class BaseDialogFragment : DialogFragment() {

    protected val dialogView: View? by lazy {
        getCustomDialogView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setView(dialogView)
                .setTitle(getTitle())
                .setPositiveButton(R.string.dialog_ok, { _, _ -> onPositive() })
                .setNegativeButton(R.string.dialog_cancel, { _, _ -> onNegative() })
                .create()
    }

    open protected fun getCustomDialogView(): View? = null

    open protected fun getTitle(): String? = null

    abstract fun onPositive()

    abstract fun onNegative()
}