package io.github.vladimirmi.radius.model.interactor

import io.github.vladimirmi.radius.model.entity.PlayerMode
import io.github.vladimirmi.radius.model.repository.StationListRepository
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class PlayerControlsInteractor
@Inject constructor(private val repository: StationListRepository) {

    private var enableNextPreviousListener: ((Boolean) -> Unit)? = null

    private val enableNextPreviousObs = Observable.create<PlayerMode> { e ->
        enableNextPreviousListener = { enabled ->
            if (!e.isDisposed) {
                if (enabled) e.onNext(PlayerMode.NEXT_PREVIOUS_ENABLED)
                else e.onNext(PlayerMode.NEXT_PREVIOUS_DISABLED)
            }
        }
        e.setDisposable(Disposables.fromRunnable { enableNextPreviousListener = null })
    }

    val playerModeObs: Observable<PlayerMode> = repository.stationList.observe()
            .map {
                if (it.itemsSize > 1) {
                    PlayerMode.NEXT_PREVIOUS_ENABLED
                } else {
                    PlayerMode.NEXT_PREVIOUS_DISABLED
                }
            }.mergeWith(enableNextPreviousObs)

    fun tryEnableNextPrevious(enable: Boolean) {
        enableNextPreviousListener?.invoke(enable && repository.stationList.itemsSize > 1)
    }

    fun nextStation(cycle: Boolean = true): Boolean {
        val next = repository.stationList.getNext(repository.currentStation.value, cycle)
        return if (next != null) {
            repository.setCurrentStation(next)
            true
        } else false
    }

    fun previousStation(cycle: Boolean = true): Boolean {
        val previous = repository.stationList.getPrevious(repository.currentStation.value, cycle)
        return if (previous != null) {
            repository.setCurrentStation(previous)
            true
        } else false
    }
}