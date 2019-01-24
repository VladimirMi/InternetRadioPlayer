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

/**
 * Created by Vladimir Mikhalev 18.01.2019.
 */

interface PresetBinderView {

    val iconResId: Int
    val descriptionResId: Int
}

abstract class PresetBinder(val station: Station,
                            val group: Group,
                            var presetName: String) : PresetBinderView {


    open fun bind(db: StationsDatabase, prefs: Preferences): Completable {
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
            return if (station.equalizerPreset == null) {
                if (group.equalizerPreset == null) {
                    val globalPreset = Scopes.app.getInstance(Preferences::class.java).globalPreset
                    GlobalPresetBinder(station, group, globalPreset)
                } else {
                    GroupPresetBinder(station, group, group.equalizerPreset)
                }
            } else {
                StationPresetBinder(station, group, station.equalizerPreset)
            }
        }
    }
}

class StationPresetBinder(station: Station, group: Group, presetName: String)
    : PresetBinder(station, group, presetName) {

    override val iconResId = R.drawable.ic_station_1
    override val descriptionResId = R.string.preset_bind_station

    override fun nextBinder(): GlobalPresetBinder {
        return GlobalPresetBinder(
                station.copy(equalizerPreset = null),
                group.copy(equalizerPreset = null),
                presetName)
    }

    override fun toString(): String {
        return "StationPresetBinder(presetName='$presetName')"
    }
}

class GroupPresetBinder(station: Station, group: Group, presetName: String)
    : PresetBinder(station, group, presetName) {

    override val iconResId = R.drawable.ic_group
    override val descriptionResId = R.string.preset_bind_group

    override fun nextBinder() = StationPresetBinder(
            station.copy(equalizerPreset = presetName),
            group.copy(equalizerPreset = null),
            presetName
    )

    override fun toString(): String {
        return "GroupPresetBinder(presetName='$presetName')"
    }
}

class GlobalPresetBinder(station: Station, group: Group, presetName: String)
    : PresetBinder(station, group, presetName) {

    override val iconResId = R.drawable.ic_globe
    override val descriptionResId = R.string.preset_bind_all

    override fun bind(db: StationsDatabase, prefs: Preferences): Completable {
        return super.bind(db, prefs)
                .doOnComplete { prefs.globalPreset = presetName }
    }

    override fun nextBinder() = GroupPresetBinder(
            station.copy(equalizerPreset = null),
            group.copy(equalizerPreset = presetName),
            presetName
    )

    override fun toString(): String {
        return "GlobalPresetBinder(presetName='$presetName')"
    }

}