package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.records

import io.github.vladimirmi.internetradioplayer.domain.interactor.MediaInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.RecordsInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

class RecordsPresenter
@Inject constructor(private val recordsInteractor: RecordsInteractor,
                    private val mediaInteractor: MediaInteractor)
    : BasePresenter<RecordsView>() {

    override fun onAttach(view: RecordsView) {
        recordsInteractor.recordsObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.setRecords(it) })
                .addTo(viewSubs)

        mediaInteractor.currentMediaObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectRecord(it.id) })
                .addTo(viewSubs)
    }

    fun selectRecord(record: Record) {
        mediaInteractor.currentMedia = record
    }

    fun deleteRecord(record: Record) {
        recordsInteractor.deleteRecord(record)
                .subscribeX()
                .addTo(dataSubs)
    }
}