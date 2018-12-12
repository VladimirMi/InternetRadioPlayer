package io.github.vladimirmi.internetradioplayer.presentation.base

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import io.github.vladimirmi.internetradioplayer.R

/**
 * Created by Vladimir Mikhalev 22.12.2017.
 */


abstract class BaseDialogFragment : DialogFragment() {

    protected val dialogView: View? by lazy {
        val view = getCustomDialogView()
        if (view != null && view.parent != null) {
            (view.parent as ViewGroup).removeView(view)
        }
        view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setTitle(getTitle())
                .setPositiveButton(R.string.dialog_ok) { _, _ -> onPositive() }
                .setNegativeButton(R.string.dialog_cancel) { _, _ -> onNegative() }
                .create()
    }

    protected open fun getCustomDialogView(): View? = null

    protected abstract fun getTitle(): String

    protected abstract fun onPositive()

    protected abstract fun onNegative()
}
