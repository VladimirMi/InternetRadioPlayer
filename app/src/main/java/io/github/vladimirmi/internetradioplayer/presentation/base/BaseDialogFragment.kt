package io.github.vladimirmi.internetradioplayer.presentation.base

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import io.github.vladimirmi.internetradioplayer.R

/**
 * Created by Vladimir Mikhalev 22.12.2017.
 */


abstract class BaseDialogFragment : DialogFragment() {

    protected var dialogView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = getCustomDialogView()
        return AlertDialog.Builder(requireContext())
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
