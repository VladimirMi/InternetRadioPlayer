package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_edit.view.*

/**
 * Created by Vladimir Mikhalev 31.12.2017.
 */

class EditDialog : BaseDialogFragment() {

    companion object {
        private const val TITLE = "TITLE"
        private const val HINT = "HINT"
        private const val TEXT = "TEXT"

        fun newInstance(title: String, hint: String, text: String): EditDialog {
            return EditDialog().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                    putString(HINT, hint)
                    putString(TEXT, text)
                }
            }
        }
    }

    override fun getTitle(): String {
        return arguments?.getString(TITLE) ?: ""
    }

    @SuppressLint("InflateParams")
    override fun getCustomDialogView(): View? {
        return LayoutInflater.from(context).inflate(R.layout.dialog_edit, null).apply {
            editTextView.hint = arguments?.getString(HINT) ?: ""
            editTextView.setText(arguments?.getString(TEXT) ?: "")
            editTextView.setSelection(editTextView.text.length)
        }
    }

    override fun onPositive() {
        (parentFragment as? Callback)?.onDialogEdit(dialogView!!.editTextView.text.toString(), tag
                ?: "")
    }

    override fun onNegative() {
    }

    interface Callback {
        fun onDialogEdit(newText: String, tag: String)
    }
}
