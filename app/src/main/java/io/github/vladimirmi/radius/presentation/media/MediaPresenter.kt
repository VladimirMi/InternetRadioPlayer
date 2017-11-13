package io.github.vladimirmi.radius.presentation.media

import android.support.v4.media.session.PlaybackStateCompat
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.MediaBrowserController
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

@InjectViewState
class MediaPresenter
@Inject constructor(private val repository: StationRepository,
                    private val mediaBrowserController: MediaBrowserController)
    : BasePresenter<MediaView>() {


    override fun onFirstViewAttach() {
        repository.groupedStationList.observe()
                .subscribeBy { viewState.setMediaList(it) }
                .addTo(compDisp)

        repository.selected
                .subscribeBy { viewState.select(it, playing = false) }
                .addTo(compDisp)

        mediaBrowserController.playbackState
                .subscribeBy {
                    val station = repository.selected.value ?: return@subscribeBy
                    if (it.state == PlaybackStateCompat.STATE_PLAYING) {
                        viewState.select(station, playing = true)
                    } else {
                        viewState.select(station, playing = false)
                    }
                }.addTo(compDisp)
    }

    fun select(station: Station) {
        repository.setSelected(station)
    }

    fun selectGroup(group: String) {
        if (repository.groupedStationList.isGroupVisible(group)) {
            repository.groupedStationList.hideGroup(group)
        } else {
            repository.groupedStationList.showGroup(group)
        }
        viewState.notifyList()
    }
}


