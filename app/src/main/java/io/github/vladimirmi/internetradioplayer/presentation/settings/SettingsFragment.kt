package io.github.vladimirmi.internetradioplayer.presentation.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ShareCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.utils.BACKUP_TYPE
import io.github.vladimirmi.internetradioplayer.data.utils.BackupRestoreHelper
import io.github.vladimirmi.internetradioplayer.data.utils.Preferences
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.github.vladimirmi.internetradioplayer.extensions.startActivitySafe
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BackPressListener
import io.github.vladimirmi.internetradioplayer.presentation.navigation.Router
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by Vladimir Mikhalev 30.09.2018.
 */

private const val PICK_BACKUP_REQUEST_CODE = 999

class SettingsFragment : PreferenceFragmentCompat(), BackPressListener {

    private val backupRestoreHelper = Scopes.app.getInstance(BackupRestoreHelper::class.java)
    private val router = Scopes.rootActivity.getInstance(Router::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = Preferences.PREFERENCES_NAME
        addPreferencesFromResource(R.xml.settings_screen)

        findPreference<Preference>("BACKUP_STATIONS")?.setOnPreferenceClickListener {
            val uri = backupRestoreHelper.createBackup()
            val intent = ShareCompat.IntentBuilder.from(activity)
                    .setType(BACKUP_TYPE)
                    .setSubject(getString(R.string.full_app_name))
                    .setStream(uri)
                    .setChooserTitle(getString(R.string.chooser_save))
                    .createChooserIntent()
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            requireContext().startActivitySafe(intent)
            true
        }
        findPreference<Preference>("RESTORE_STATIONS")?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = BACKUP_TYPE
            if (requireContext().packageManager.resolveActivity(intent, 0) != null) {
                startActivityForResult(intent, PICK_BACKUP_REQUEST_CODE)
            }
            true
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is SeekBarDialogPreference) {
            val fragment = SeekBarDialogFragment.newInstance(preference.key)
            fragment.setTargetFragment(this, 0)
            fragment.show(requireFragmentManager(), "SeekBarDialogFragment")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_BACKUP_REQUEST_CODE && resultCode == Activity.RESULT_OK && data?.data != null) {
            //todo to interactor
            backupRestoreHelper.restoreBackup(requireContext().contentResolver.openInputStream(data.data!!)!!)
                    .andThen(Scopes.app.getInstance(FavoriteListInteractor::class.java).initFavoriteList())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeX(onComplete = { router.exit() })
        }
    }

    override fun handleBackPressed(): Boolean {
        router.exit()
        return true
    }
}
