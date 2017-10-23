package io.github.vladimirmi.radius.presentation.playercontrol

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.github.vladimirmi.radius.data.repository.MediaBrowserController
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

@InjectViewState
class PlayerControlPresenter
@Inject constructor(private val browserController: MediaBrowserController)
    : MvpPresenter<PlayerControlView>() {

    private val callback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            Timber.e("onPlaybackStateChanged: $state")
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            Timber.e("onMetadataChanged: ${metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)}: " +
                    " ${metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)} - " +
                    " ${metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)}")
        }
    }

    override fun onFirstViewAttach() {
        browserController.registerCallback(callback)
    }

    override fun onDestroy() {
        browserController.unRegisterCallback(callback)
    }
}