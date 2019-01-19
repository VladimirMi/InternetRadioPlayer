package io.github.vladimirmi.internetradioplayer.domain.model

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.StationsDatabase
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 18.01.2019.
 */

interface PresetBinderView {

    val iconResId: Int
    val descriptionResId: Int
}

abstract class PresetBinder(protected val db: StationsDatabase) : PresetBinderView {

    abstract fun bind(): Completable

    abstract fun nextBinder(): PresetBinder

    fun switch(): Single<PresetBinder> {
        val binder = nextBinder()
        return binder.bind()
                .andThen(Single.just(binder))
    }

    companion object {
        fun create(db: StationsDatabase, stationId: String): Single<Pair<String, PresetBinder>> {
            val dao = db.stationDao()

            return dao.getStation(stationId).flatMap { station ->
                if (station.equalizerPreset == null) {
                    dao.getGroup(station.groupId).flatMap { group ->
                        if (group.equalizerPreset == null) Single.just("" to GlobalPresetBinder(db))
                        else Single.just(group.equalizerPreset to GroupPresetBinder(db))
                    }
                } else {
                    Single.just(station.equalizerPreset to StationPresetBinder(db))
                }
            }
        }
    }
}

class StationPresetBinder(db: StationsDatabase) : PresetBinder(db) {

    override val iconResId = R.drawable.ic_station_1
    override val descriptionResId = R.string.preset_bind_station

    override fun bind(): Completable {
        return Completable.fromAction { Timber.e("bind: StationPresetBinder") }
    }

    override fun nextBinder() = GlobalPresetBinder(db)
}

class GroupPresetBinder(db: StationsDatabase) : PresetBinder(db) {

    override val iconResId = R.drawable.ic_group
    override val descriptionResId = R.string.preset_bind_group

    override fun bind(): Completable {
        return Completable.fromAction { Timber.e("bind: GroupPresetBinder") }
    }

    override fun nextBinder() = StationPresetBinder(db)
}

class GlobalPresetBinder(db: StationsDatabase) : PresetBinder(db) {

    override val iconResId = R.drawable.ic_globe
    override val descriptionResId = R.string.preset_bind_all

    override fun bind(): Completable {
        return Completable.fromAction { Timber.e("bind: GlobalPresetBinder") }
    }

    override fun nextBinder() = GroupPresetBinder(db)
}