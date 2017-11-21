package io.github.vladimirmi.radius.presentation.station

import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.ui.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

@InjectViewState
class StationPresenter
@Inject constructor(private val repository: StationRepository)
    : BasePresenter<StationView>() {

    lateinit var id: String

    override fun onFirstViewAttach() {
        viewState.setStation(repository.getStation(id))
    }
}