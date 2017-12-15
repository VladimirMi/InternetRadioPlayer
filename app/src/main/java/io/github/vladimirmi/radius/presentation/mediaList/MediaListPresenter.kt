package io.github.vladimirmi.radius.presentation.mediaList

import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.MediaBrowserController
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

@InjectViewState
class MediaListPresenter
@Inject constructor(private val repository: StationRepository,
                    private val mediaBrowserController: MediaBrowserController,
                    private val router: Router)
    : BasePresenter<MediaListView>() {

    val builder get() = ToolbarBuilder()

    override fun onFirstViewAttach() {
        builder.setToolbarTitleId(R.string.app_name)
        viewState.buildToolbar(builder)

        repository.groupedStationList.observe()
                .subscribeBy { viewState.setMediaList(it) }
                .addTo(compDisp)

        repository.current
                .subscribeBy {
                    viewState.selectItem(it, mediaBrowserController.isPlaying)
                    viewState.buildToolbar(builder.setToolbarTitle(it.title))
                }
                .addTo(compDisp)

        mediaBrowserController.playbackState
                .subscribeBy {
                    val station = repository.current.value
                    if (mediaBrowserController.isPlaying) {
                        viewState.selectItem(station, playing = true)
                    } else {
                        viewState.selectItem(station, playing = false)
                    }
                }.addTo(compDisp)
    }

    fun select(station: Station) {
        repository.setCurrent(station)
    }

    fun selectGroup(group: String) {
        if (repository.groupedStationList.isGroupVisible(group)) {
            repository.groupedStationList.hideGroup(group)
        } else {
            repository.groupedStationList.showGroup(group)
        }
        viewState.notifyList()
    }

    fun removeStation(station: Station) {
        viewState.openRemoveDialog(station)
    }

    fun submitRemove(station: Station) {
        repository.remove(station)
        viewState.closeRemoveDialog()
    }

    fun cancelRemove() {
        repository.groupedStationList.notifyObservers()
        viewState.closeRemoveDialog()
    }

    fun showStation(station: Station) {
        router.showStation(station)
    }
}


