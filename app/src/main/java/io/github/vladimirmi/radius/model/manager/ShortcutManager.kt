package io.github.vladimirmi.radius.model.manager

import android.content.Context
import android.content.Intent
import android.support.v4.content.pm.ShortcutInfoCompat
import android.support.v4.content.pm.ShortcutManagerCompat
import android.support.v4.graphics.drawable.IconCompat
import io.github.vladimirmi.radius.model.entity.Icon
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.service.PlayerService
import io.github.vladimirmi.radius.ui.SplashActivity
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.01.2018.
 */

class ShortcutManager
@Inject constructor(private val context: Context) {

    fun pinShortcut(station: Station, icon: Icon): Boolean {
        val id = "${station.name} shortcut"
        val shortcutIcon = IconCompat.createWithBitmap(icon.bitmap)

        val intent = Intent(context, SplashActivity::class.java).apply {
            putExtra(PlayerService.EXTRA_STATION_ID, station.id)
        }

        val info = ShortcutInfoCompat.Builder(context, id)
                .setShortLabel(station.name)
                .setLongLabel(station.name)
                .setIcon(shortcutIcon)
                .setIntent(intent)
                .build()

        return ShortcutManagerCompat.requestPinShortcut(context, info, null)
    }
}