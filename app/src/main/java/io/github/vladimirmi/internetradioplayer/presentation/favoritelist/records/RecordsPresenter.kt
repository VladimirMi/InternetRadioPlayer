package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.records

import io.github.vladimirmi.internetradioplayer.domain.interactor.RecordsInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

class RecordsPresenter
@Inject constructor(private val recordsInteractor: RecordsInteractor)
    : BasePresenter<RecordsView>() {

    override fun onAttach(view: RecordsView) {
        recordsInteractor.recordsObs
                .subscribeX(onNext = { view.setRecords(it) })
                .addTo(viewSubs)
    }
}