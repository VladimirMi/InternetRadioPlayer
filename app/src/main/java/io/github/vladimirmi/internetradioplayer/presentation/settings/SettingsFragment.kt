package io.github.vladimirmi.internetradioplayer.presentation.settings

import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.ui.SeekBarDialogPreference
import android.content.Intent
import android.support.v4.app.ShareCompat
import io.github.vladimirmi.internetradioplayer.data.manager.BackupRestoreHelper
import timber.log.Timber


/**
 * Created by Vladimir Mikhalev 30.09.2018.
 */

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_screen)

        val backupRestoreHelper = Scopes.app.getInstance(BackupRestoreHelper::class.java)
        findPreference("BACKUP_STATIONS").setOnPreferenceClickListener {
            val uri = backupRestoreHelper.createBackup()
            val intent = ShareCompat.IntentBuilder.from(activity)
                    .setType("text/xml")
                    .setSubject("Subject")
                    .setStream(uri)
                    .setChooserTitle("Chooser title")
                    .createChooserIntent()
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context!!.startActivity(intent)
            true
        }
        findPreference("RESTORE_STATIONS").setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "text/xml"
            startActivityForResult(intent, 999)

            true
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.e("onActivityResult: $requestCode $resultCode $data")
    }
}
