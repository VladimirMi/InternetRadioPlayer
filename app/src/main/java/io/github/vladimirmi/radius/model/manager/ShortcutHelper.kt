package io.github.vladimirmi.radius.model.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import android.support.v4.content.pm.ShortcutInfoCompat
import android.support.v4.content.pm.ShortcutManagerCompat
import android.support.v4.graphics.drawable.IconCompat
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.entity.Icon
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.service.PlayerService
import io.github.vladimirmi.radius.presentation.root.RootActivity
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.01.2018.
 */

class ShortcutHelper
@Inject constructor(private val context: Context) {

    fun pinShortcut(station: Station, icon: Icon): Boolean {

        val info = ShortcutInfoCompat.Builder(context, station.id)
                .setShortLabel(station.name)
                .setLongLabel(station.name)
                .setIcon(IconCompat.createWithBitmap(icon.bitmap))
                .setIntent(createShortcutIntent(station))
                .setDisabledMessage(context.getString(R.string.toast_shortcut_remove))
                .build()

        return ShortcutManagerCompat.requestPinShortcut(context, info, null)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    fun removeShortcut(station: Station) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            shortcutManager.disableShortcuts(listOf(station.id), context.getString(R.string.toast_shortcut_remove))
        } else {
            val removeIntent = Intent().apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, createShortcutIntent(station))
                putExtra(Intent.EXTRA_SHORTCUT_NAME, station.name)
                action = "com.android.launcher.action.UNINSTALL_SHORTCUT"
            }
            context.sendBroadcast(removeIntent)
        }
    }

    private fun createShortcutIntent(station: Station): Intent {
        return Intent(context, RootActivity::class.java).apply {
            putExtra(PlayerService.EXTRA_STATION_ID, station.id)
            putExtra("duplicate", false)
            action = Intent.ACTION_MAIN
        }
    }
}