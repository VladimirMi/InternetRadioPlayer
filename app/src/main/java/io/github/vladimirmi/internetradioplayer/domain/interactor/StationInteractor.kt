package io.github.vladimirmi.internetradioplayer.domain.interactor

import android.net.Uri
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.FavoritesRepository
import io.github.vladimirmi.internetradioplayer.data.repository.MediaRepository
import io.github.vladimirmi.internetradioplayer.data.repository.StationRepository
import io.github.vladimirmi.internetradioplayer.data.utils.ShortcutHelper
import io.github.vladimirmi.internetradioplayer.utils.MessageResException
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toCompletable
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class StationInteractor
@Inject constructor(private val stationRepository: StationRepository,
                    private val favoritesRepository: FavoritesRepository,
                    private val favoriteListInteractor: FavoriteListInteractor,
                    private val mediaRepository: MediaRepository,
                    private val shortcutHelper: ShortcutHelper) {

    fun addCurrentShortcut(startPlay: Boolean): Boolean {
        val station = mediaRepository.currentMedia as? Station ?: return false
        return shortcutHelper.pinShortcut(station, startPlay)
    }

    fun createStation(uri: Uri): Single<Station> {
        return stationRepository.createStation(uri)
                .doOnSuccess { newStation ->
                    val favoriteStation = favoritesRepository.getStation { it.uri == newStation.uri }
                    mediaRepository.currentMedia = favoriteStation ?: newStation
                }
    }

    fun switchFavorite(station: Station): Completable {
        return if (isFavorite(station.id)) removeFromFavorite(station)
        else addToFavorite(station)
    }

    fun changeGroup(groupName: String): Completable {
        val station = mediaRepository.currentMedia as? Station ?: return Completable.complete()
        return Single.fromCallable { favoritesRepository.groups.find { it.name == groupName } }
                .flatMapCompletable {
                    if (it.id == station.groupId) Completable.complete()
                    else {
                        val newStation = station.copy(groupId = it.id, order = it.stations.size)
                        favoritesRepository.updateStations(listOf(newStation))
                                .andThen(setStation(newStation))
                                .andThen(favoriteListInteractor.initFavoriteList())
                    }
                }
    }

    fun updateStation(station: Station): Completable {
        if (station.name.isBlank()) return Completable.error(MessageResException(R.string.msg_name_empty_error))

        return favoritesRepository.updateStations(listOf(station))
                .andThen(favoriteListInteractor.initFavoriteList())
    }

    private fun setStation(station: Station): Completable {
        return { mediaRepository.currentMedia = station }.toCompletable()
    }

    private fun addToFavorite(station: Station): Completable {
        return favoriteListInteractor.getGroup(station.groupId)
                .flatMapCompletable {
                    val newStation = station.copy(order = it.stations.size, groupId = Group.DEFAULT_ID)
                    favoritesRepository.addStation(newStation)
                            .andThen(favoriteListInteractor.initFavoriteList())
                            .andThen(setStation(newStation))
                }
    }

    private fun removeFromFavorite(station: Station): Completable {
        return favoritesRepository.removeStation(station)
                .andThen(favoriteListInteractor.initFavoriteList())
                .andThen(setStation(station))
    }

    private fun isFavorite(id: String) = favoritesRepository.getStation { it.id == id } != null
}

