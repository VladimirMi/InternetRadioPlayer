package io.github.vladimirmi.internetradioplayer.data.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver
import io.github.vladimirmi.internetradioplayer.presentation.root.RootActivity

/**
 * Created by Vladimir Mikhalev 20.12.2017.
 */

object PlayerActions {

    const val DEFAULT_ACTIONS = (PlaybackStateCompat.ACTION_PLAY_PAUSE
            or PlaybackStateCompat.ACTION_STOP
            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)

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
