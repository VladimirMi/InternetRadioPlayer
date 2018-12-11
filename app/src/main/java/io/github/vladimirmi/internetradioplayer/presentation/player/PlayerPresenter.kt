package io.github.vladimirmi.internetradioplayer.presentation.player

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import io.github.vladimirmi.internetradioplayer.data.service.album
import io.github.vladimirmi.internetradioplayer.data.service.notSupported
import io.github.vladimirmi.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class PlayerPresenter
@Inject constructor(private val stationInteractor: StationInteractor,
                    private val favoriteListInteractor: FavoriteListInteractor,
                    private val playerInteractor: PlayerInteractor)
    : BasePresenter<PlayerView>() {

    override fun onAttach(view: PlayerView) {
        setupStation()
        setupGroups()
        setupPlayer()
    }

    private fun setupStation() {
        stationInteractor.stationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = {
                    view?.setStation(it)
                    view?.setFavorite(favoriteListInteractor.isFavorite(it))
                })
                .addTo(viewSubs)
    }

    private fun setupGroups() {
        val groupObs = stationInteractor.stationObs
                .flatMapSingle { favoriteListInteractor.getGroup(it.groupId) }
                .map { it.name }
                .observeOn(AndroidSchedulers.mainThread())

        val groupsObs = favoriteListInteractor.getGroupsObs()
                .map { groups -> groups.map { it.name } }
                .observeOn(AndroidSchedulers.mainThread())

        Observables.combineLatest(groupsObs, groupObs) { list, group ->
            view?.setGroups(list)
            list.indexOf(group) + 1 //new folder option offset
        }.subscribeX(onNext = { view?.setGroup(it) })
                .addTo(viewSubs)
    }

    private fun setupPlayer() {
        playerInteractor.playbackStateObs
                .subscribeX(onNext = { handleState(it) })
                .addTo(viewSubs)

        playerInteractor.metadataObs
                .subscribeX(onNext = { handleMetadata(it) })
                .addTo(viewSubs)

        playerInteractor.sessionEventObs
                .subscribeX(onNext = { handleSessionEvent(it) })
                .addTo(viewSubs)
    }

    fun switchFavorite() {
        val isFavorite = favoriteListInteractor.isFavorite(stationInteractor.station)
        val changeFavorite = if (isFavorite) stationInteractor.removeFromFavorite()
        else stationInteractor.addToFavorite()
        changeFavorite.observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onComplete = { view?.setFavorite(!isFavorite) })
                .addTo(viewSubs)
    }

    fun selectGroup(position: Int, group: String) {
        if (position == 0) view?.openNewGroupDialog()
        else stationInteractor.changeGroup(group)
                .subscribeX()
                .addTo(viewSubs)
    }


    fun createGroup(groupName: String) {
        favoriteListInteractor.createGroup(groupName)
                .andThen(stationInteractor.changeGroup(groupName))
                .subscribeX()
                .addTo(viewSubs)
    }

    fun editStationTitle(title: String) {
        stationInteractor.editStationTitle(title)
                .subscribeX()
                .addTo(viewSubs)
    }

    fun addShortcut(startPlay: Boolean) {
        if (stationInteractor.addCurrentShortcut(startPlay)) {
            view?.showMessage(R.string.msg_add_shortcut_success)
        }
    }

    fun playPause() {
        with(playerInteractor) {
            if (!isPlaying && !isNetAvail) {
                view?.showMessage(R.string.msg_net_error)
            } else {
                playPause()
            }
        }
    }

    fun stop() {
        playerInteractor.stop()
    }

    fun skipToPrevious() {
        playerInteractor.skipToPrevious()
    }

    fun skipToNext() {
        playerInteractor.skipToNext()
    }

    private fun handleState(state: PlaybackStateCompat) {
        when (state.state) {
            PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> view?.showStopped()
            PlaybackStateCompat.STATE_BUFFERING -> view?.showBuffering()
            PlaybackStateCompat.STATE_PLAYING -> view?.showPlaying()
        }
    }

    private fun handleMetadata(metadata: MediaMetadataCompat) {
        //todo fix
//        if (metadata.notSupported()&&metadata.notEmpty()) viewState.setMetadata(metadata.album!!)
        if (metadata.notSupported() && metadata.album != null) view?.setMetadata(null)
        else view?.setMetadata(metadata)
    }

    private fun handleSessionEvent(event: String) {
        when (event) {
            PlayerService.EVENT_SESSION_PREVIOUS -> view?.showPrevious()
            PlayerService.EVENT_SESSION_NEXT -> view?.showNext()
        }
    }
}
