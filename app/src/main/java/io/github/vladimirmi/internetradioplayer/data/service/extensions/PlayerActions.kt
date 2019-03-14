package io.github.vladimirmi.internetradioplayer.data.service.extensions

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver
import io.github.vladimirmi.internetradioplayer.presentation.root.RootActivity

/**
 * Created by Vladimir Mikhalev 20.12.2017.
 */

@Suppress("MemberVisibilityCanBePrivate")
object PlayerActions {

    const val DEFAULT_ACTIONS = (PlaybackStateCompat.ACTION_PLAY_PAUSE
            or PlaybackStateCompat.ACTION_STOP
            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            or PlaybackStateCompat.ACTION_SEEK_TO)

    fun isSeekEnabled(sourceActions: Long): Boolean {
        return haveActions(sourceActions, PlaybackStateCompat.ACTION_SEEK_TO)
    }

    fun isSkipEnabled(sourceActions: Long): Boolean {
        return haveActions(sourceActions, PlaybackStateCompat.ACTION_SKIP_TO_NEXT,
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
    }

    fun haveActions(sourceActions: Long, vararg actions: Long): Boolean {
        for (action in actions) {
            if (sourceActions and action != action) return false
        }
        return true
    }

    fun enableSkip(actions: Long): Long {
        return (actions or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
    }

    fun disableSkip(actions: Long): Long {
        return actions and (PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).inv()
    }

    fun enableSeek(actions: Long): Long {
        return actions or PlaybackStateCompat.ACTION_SEEK_TO
    }

    fun disableSeek(actions: Long): Long {
        return actions and (PlaybackStateCompat.ACTION_SEEK_TO).inv()
    }

    fun playPauseIntent(context: Context): PendingIntent {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                PlaybackStateCompat.ACTION_PLAY_PAUSE)
    }

    fun stopIntent(context: Context): PendingIntent {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                PlaybackStateCompat.ACTION_STOP)
    }

    fun nextIntent(context: Context): PendingIntent {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
    }

    fun previousIntent(context: Context): PendingIntent {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
    }

    fun sessionActivity(context: Context): PendingIntent {
        return PendingIntent.getActivity(context, 0, Intent(context, RootActivity::class.java), 0)
    }
}
