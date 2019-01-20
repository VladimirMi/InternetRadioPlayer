package io.github.vladimirmi.internetradioplayer.domain.model

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.StationsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.dao.StationDao
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.utils.Preferences
import io.github.vladimirmi.internetradioplayer.di.Scopes
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

abstract class PresetBinder(val station: Station,
                            val group: Group) : PresetBinderView {

    protected val db: StationsDatabase = Scopes.app.getInstance(StationsDatabase::class.java)
    protected val prefs: Preferences = Scopes.app.getInstance(Preferences::class.java)

    abstract var presetName: String

    open fun bind(): Completable {
        return Completable.fromAction {
            db.runInTransaction {
                with(db.stationDao()) {
                    updateStation(station)
                    updateGroup(group)
                }
            }
        }
    }

    abstract fun nextBinder(): PresetBinder

    companion object {
        fun create(dao: StationDao, stationId: String): Single<PresetBinder> {
            return dao.getStation(stationId)
                    .flatMap { station ->
                        dao.getGroup(station.groupId)
                                .map { group -> createBinder(station, group) }
                    }
        }

        private fun createBinder(station: Station, group: Group): PresetBinder {
            Timber.e("createBinder: s-${station.equalizerPreset} g-${group.equalizerPreset}")
            return if (station.equalizerPreset == null) {
                if (group.equalizerPreset == null) {
                    GlobalPresetBinder(station, group)
                } else {
                    GroupPresetBinder(station, group)
                }
            } else {
                StationPresetBinder(station, group)
            }
        }
    }
}

class StationPresetBinder(station: Station, group: Group) : PresetBinder(station, group) {

    override val iconResId = R.drawable.ic_station_1
    override val descriptionResId = R.string.preset_bind_station
    override var presetName = station.equalizerPreset!!

    override fun nextBinder(): GlobalPresetBinder {
        return GlobalPresetBinder(
                station.copy(equalizerPreset = null),
                group.copy(equalizerPreset = null)
        ).apply { presetName = this@StationPresetBinder.presetName }
    }

    override fun toString(): String {
        return "StationPresetBinder(presetName='$presetName')"
    }
}

class GroupPresetBinder(station: Station, group: Group) : PresetBinder(station, group) {

    override val iconResId = R.drawable.ic_group
    override val descriptionResId = R.string.preset_bind_group
    override var presetName = group.equalizerPreset!!

    override fun nextBinder() = StationPresetBinder(
            station.copy(equalizerPreset = presetName),
            group.copy(equalizerPreset = null)
    )

    override fun toString(): String {
        return "GroupPresetBinder(presetName='$presetName')"
    }
}

class GlobalPresetBinder(station: Station, group: Group) : PresetBinder(station, group) {

    override val iconResId = R.drawable.ic_globe
    override val descriptionResId = R.string.preset_bind_all
    override var presetName = prefs.globalPreset

    override fun bind(): Completable {
        return super.bind()
                .doOnComplete { prefs.globalPreset = presetName }
    }

    override fun nextBinder() = GroupPresetBinder(
            station.copy(equalizerPreset = null),
            group.copy(equalizerPreset = presetName)
    )

    override fun toString(): String {
        return "GlobalPresetBinder(presetName='$presetName')"
    }

}