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


    abstract fun bind(db: StationsDatabase, prefs: Preferences): Completable

    abstract fun nextBinder(): PresetBinder

    companion object {
        fun create(dao: StationDao, stationId: String): Single<PresetBinder> {
            return dao.getStationMaybe(stationId)
                    .flatMap { station ->
                        dao.getGroupMaybe(station.groupId)
                                .map { group -> createBinder(station, group) }
                    }.toSingle(createBinder(Station.nullObj(), Group.nullObj()))
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

    override fun nextBinder() = GlobalPresetBinder(station, group, presetName)

    override fun bind(db: StationsDatabase, prefs: Preferences): Completable {
        return Completable.fromAction {
            db.runInTransaction {
                with(db.stationDao()) {
                    updateStation(station.copy(equalizerPreset = presetName))
                    updateGroup(group)
                }
            }
        }
    }

    override fun toString(): String {
        return "StationPresetBinder(presetName='$presetName')"
    }
}

class GroupPresetBinder(station: Station, group: Group, presetName: String)
    : PresetBinder(station, group, presetName) {

    override val iconResId = R.drawable.ic_group
    override val descriptionResId = R.string.preset_bind_group

    override fun nextBinder() = StationPresetBinder(station, group, presetName)

    override fun bind(db: StationsDatabase, prefs: Preferences): Completable {
        return Completable.fromAction {
            db.runInTransaction {
                with(db.stationDao()) {
                    updateStation(station.copy(equalizerPreset = null))
                    updateGroup(group.copy(equalizerPreset = presetName))
                }
            }
        }
    }

    override fun toString(): String {
        return "GroupPresetBinder(presetName='$presetName')"
    }
}

class GlobalPresetBinder(station: Station, group: Group, presetName: String)
    : PresetBinder(station, group, presetName) {

    override val iconResId = R.drawable.ic_globe
    override val descriptionResId = R.string.preset_bind_all

    override fun nextBinder(): PresetBinder {
        return if (station.isNull()) this
        else GroupPresetBinder(station, group, presetName)
    }

    override fun bind(db: StationsDatabase, prefs: Preferences): Completable {
        return Completable.fromAction {
            db.runInTransaction {
                with(db.stationDao()) {
                    if (!station.isNull()) updateStation(station.copy(equalizerPreset = null))
                    if (!group.isNull()) updateGroup(group.copy(equalizerPreset = null))
                }
            }
        }.doOnComplete { prefs.globalPreset = presetName }
    }

    override fun toString(): String {
        return "GlobalPresetBinder(presetName='$presetName')"
    }

}