package io.github.vladimirmi.internetradioplayer.presentation.settings

import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.ui.SeekBarDialogPreference

/**
 * Created by Vladimir Mikhalev 30.09.2018.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_screen)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is SeekBarDialogPreference) {
            val fragment = SeekBarDialogFragment.newInstance(preference.key)
            fragment.setTargetFragment(this, 0)
            fragment.show(fragmentManager, "SeekBarDialogFragment")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

}
