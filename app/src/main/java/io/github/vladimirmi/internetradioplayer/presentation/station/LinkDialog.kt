package io.github.vladimirmi.internetradioplayer.presentation.station

import android.content.Intent
import android.os.Bundle
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.toUri
import io.github.vladimirmi.internetradioplayer.ui.base.BaseDialogFragment

/**
 * Created by Vladimir Mikhalev 31.12.2017.
 */

class LinkDialog : BaseDialogFragment() {

    companion object {
        private const val KEY_URL = "KEY_URL"

        fun newInstance(url: String): LinkDialog {
            val bundle = Bundle().apply { putString(KEY_URL, url) }
            return LinkDialog().apply { arguments = bundle }
        }
    }

    override fun getTitle(): String {
        return getString(R.string.dialog_goto_message)
    }

    override fun onPositive() {
        val url = arguments!!.getString(KEY_URL)
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    override fun onNegative() {
    }
}
