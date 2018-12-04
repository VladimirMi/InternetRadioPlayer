package io.github.vladimirmi.internetradioplayer.data.repository

import io.github.vladimirmi.internetradioplayer.data.db.HistoryDatabase
import io.github.vladimirmi.internetradioplayer.data.db.entity.History
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */
private const val HISTORY_LIMIT = 50

class HistoryRepository
@Inject constructor(private val db: HistoryDatabase) {

    private val dao = db.historyDao()
    private var historySize = 0
    private lateinit var oldestEntry: History

    fun getHistoryObs(): Observable<List<Station>> {
        return dao.getHistory()
                .map { history ->
                    if (history.isNotEmpty()) oldestEntry = history.last()
                    historySize = history.size
                    history.map { it.station }
                }
                .subscribeOn(Schedulers.io())
    }

    fun createHistory(station: Station): Completable {
        return Completable.fromCallable {
            db.runInTransaction {
                if (historySize == HISTORY_LIMIT) dao.delete(oldestEntry)
                val history = History(System.currentTimeMillis(), station)
                dao.insert(history)
            }
        }.subscribeOn(Schedulers.io())
    }
}