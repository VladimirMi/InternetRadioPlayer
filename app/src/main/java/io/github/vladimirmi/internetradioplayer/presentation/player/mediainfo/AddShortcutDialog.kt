package io.github.vladimirmi.internetradioplayer.presentation.player.mediainfo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.snackbar.Snackbar
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_add_shortcut.view.*

/**
 * Created by Vladimir Mikhalev 18.09.2018.
 */

class AddShortcutDialog : BaseDialogFragment() {

    override fun getTitle(): String {
        return getString(R.string.desc_create_shortcut_button)
    }

    @SuppressLint("InflateParams")
    override fun getCustomDialogView(): View? {
        return LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_shortcut, null)
    }

    override fun onPositive() {
        dialogView?.let {
            if (Scopes.app.getInstance(StationInteractor::class.java)
                            .addCurrentShortcut(it.checkbox.isChecked)) {
                Snackbar.make(it, R.string.msg_add_shortcut_success, Snackbar.LENGTH_SHORT).show()
            } else {
                //todo describe error
                Snackbar.make(it, "Error", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNegative() {
    }
}
