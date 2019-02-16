package io.github.vladimirmi.internetradioplayer.data.repository

import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.utils.StationParser
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 25.11.2018.
 */

class StationRepository
@Inject constructor(private val stationParser: StationParser) {

    fun createStation(uri: Uri): Single<Station> {
        return Single.fromCallable {
            stationParser.parseFromUri(uri)
        }.subscribeOn(Schedulers.io())
    }
}