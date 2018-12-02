package io.github.vladimirmi.internetradioplayer.data.repository

import io.github.vladimirmi.internetradioplayer.data.db.HistoryDatabase
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

class HistoryRepository
@Inject constructor(db: HistoryDatabase) {

    private val dao = db.historyDao()
}