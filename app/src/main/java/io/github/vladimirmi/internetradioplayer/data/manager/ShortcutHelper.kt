package io.github.vladimirmi.internetradioplayer.data.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import android.support.v4.content.pm.ShortcutInfoCompat
import android.support.v4.content.pm.ShortcutManagerCompat
import android.support.v4.graphics.drawable.IconCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.extensions.getBitmap
import io.github.vladimirmi.internetradioplayer.extensions.toUri
import io.github.vladimirmi.internetradioplayer.presentation.root.RootActivity
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.01.2018.
 */

class ShortcutHelper
@Inject constructor(private val context: Context) {

    fun pinShortcut(station: Station, startPlay: Boolean): Boolean {

        val info = ShortcutInfoCompat.Builder(context, station.id)
                .setShortLabel(station.name)
                .setLongLabel(station.name)
                .setIcon(IconCompat.createWithBitmap(station.icon.getBitmap(context, withBackground = true)))
                .setIntent(createShortcutIntent(station, startPlay))
                .setDisabledMessage(context.getString(R.string.toast_shortcut_remove))
                .build()

        return ShortcutManagerCompat.requestPinShortcut(context, info, null)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    fun removeShortcut(station: Station) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            shortcutManager.disableShortcuts(listOf(station.id),
                    context.getString(R.string.toast_shortcut_remove))
        } else {
            //todo valid startPlay
            val removeIntent = Intent().apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, createShortcutIntent(station, true))
                putExtra(Intent.EXTRA_SHORTCUT_NAME, station.name)
                action = "com.android.launcher.action.UNINSTALL_SHORTCUT"
            }
            context.sendBroadcast(removeIntent)
        }
    }

    private fun createShortcutIntent(station: Station, startPlay: Boolean): Intent {
        return Intent(context, RootActivity::class.java).apply {
            putExtra("duplicate", false)
            putExtra(EXTRA_PLAY, startPlay)
            data = station.uri.toUri()
            action = Intent.ACTION_VIEW
        }
    }
}

const val EXTRA_PLAY = "play"
