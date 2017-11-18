package io.github.vladimirmi.radius.presentation.dialogs

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.StationRepository
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

@InjectViewState
class RemoveStationPresenter
@Inject constructor(private val repository: StationRepository)
    : MvpPresenter<RemoveStationView>() {

    var id: String? = null
    private val station: Station by lazy { repository.getStation(id!!) }

    fun ok() {
        repository.remove(station)
        viewState.close()
    }

    fun cancel() {
        repository.groupedStationList.notifyObservers()
        viewState.close()
    }
}