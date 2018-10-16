package io.github.vladimirmi.internetradioplayer.presentation.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.utils.BACKUP_TYPE
import io.github.vladimirmi.internetradioplayer.data.utils.BackupRestoreHelper
import io.github.vladimirmi.internetradioplayer.data.utils.PREFERENCES_NAME
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.ioToMain
import io.github.vladimirmi.internetradioplayer.extensions.startActivitySafe
import io.github.vladimirmi.internetradioplayer.extensions.subscribeByEx
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BackPressListener
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarBuilder
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarView
import io.github.vladimirmi.internetradioplayer.presentation.root.RootView
import io.github.vladimirmi.internetradioplayer.ui.SeekBarDialogPreference

/**
 * Created by Vladimir Mikhalev 30.09.2018.
 */

private const val PICK_BACKUP_REQUEST_CODE = 999

class SettingsFragment : PreferenceFragmentCompat(), BackPressListener {

    private val backupRestoreHelper = Scopes.app.getInstance(BackupRestoreHelper::class.java)
    private val router = Scopes.rootActivity.getInstance(Router::class.java)

    override fun onResume() {
        super.onResume()
        (activity as RootView).showControls(false)
        ToolbarBuilder.exit().build(activity as ToolbarView)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = PREFERENCES_NAME
        addPreferencesFromResource(R.xml.settings_screen)

        findPreference("BACKUP_STATIONS").setOnPreferenceClickListener {
            val uri = backupRestoreHelper.createBackup()
            val intent = ShareCompat.IntentBuilder.from(activity)
                    .setType(BACKUP_TYPE)
                    .setSubject(getString(R.string.full_app_name))
                    .setStream(uri)
                    .setChooserTitle(getString(R.string.chooser_backup))
                    .createChooserIntent()
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context!!.startActivitySafe(intent)
            true
        }
        findPreference("RESTORE_STATIONS").setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = BACKUP_TYPE
            if (context!!.packageManager.resolveActivity(intent, 0) != null) {
                startActivityForResult(intent, PICK_BACKUP_REQUEST_CODE)
            }
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
        if (requestCode == PICK_BACKUP_REQUEST_CODE && resultCode == Activity.RESULT_OK
                && data?.data != null) {
            backupRestoreHelper.restoreBackup(context!!.contentResolver.openInputStream(data.data))
                    .andThen(Scopes.app.getInstance(StationInteractor::class.java).initStations())
                    .ioToMain()
                    .doOnComplete { router.newRootScreen(Router.STATIONS_LIST_SCREEN) }
                    .subscribeByEx()
        }
    }

    override fun onBackPressed(): Boolean {
        (activity as RootView).showControls(true)
        return false
    }
}
