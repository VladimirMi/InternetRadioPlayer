package io.github.vladimirmi.internetradioplayer.domain.model

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.StationsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.dao.StationDao
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.utils.Preferences
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by Vladimir Mikhalev 18.01.2019.
 */

interface PresetBinderView {

    val iconResId: Int
    val descriptionResId: Int
}

abstract class PresetBinder(val stationId: String,
                            val isFavorite: Boolean,
                            var presetName: String) : PresetBinderView {


    abstract fun bind(db: StationsDatabase, prefs: Preferences): Completable

    abstract fun nextBinder(): PresetBinder

    companion object {
        fun create(dao: StationDao, stationId: String, globalPreset: String): Single<PresetBinder> {
            return dao.getStationMaybe(stationId)
                    .flatMap { station ->
                        dao.getGroupMaybe(station.groupId)
                                .map { group -> createBinder(station, group, globalPreset) }
                    }.toSingle(GlobalPresetBinder(stationId, false, globalPreset))
        }

        private fun createBinder(station: Station, group: Group, globalPreset: String): PresetBinder {
            return if (station.equalizerPreset == null) {
                if (group.equalizerPreset == null) {
                    GlobalPresetBinder(station.id, true, globalPreset)
                } else {
                    GroupPresetBinder(station.id, group.equalizerPreset)
                }
            } else {
                StationPresetBinder(station.id, station.equalizerPreset)
            }
        }
    }
}

class StationPresetBinder(stationId: String, presetName: String)
    : PresetBinder(stationId, true, presetName) {

    override val iconResId = R.drawable.ic_station_1
    override val descriptionResId = R.string.preset_bind_station

    override fun nextBinder() = GlobalPresetBinder(stationId, isFavorite, presetName)

    override fun bind(db: StationsDatabase, prefs: Preferences): Completable {
        return db.stationDao().getStation(stationId)
                .map { db.stationDao().updateStation(it.copy(equalizerPreset = presetName)) }
                .ignoreElement()
    }

    override fun toString(): String {
        return "StationPresetBinder(presetName='$presetName')"
    }
}

class GroupPresetBinder(stationId: String, presetName: String)
    : PresetBinder(stationId, true, presetName) {

    override val iconResId = R.drawable.ic_group
    override val descriptionResId = R.string.preset_bind_group

    override fun nextBinder() = StationPresetBinder(stationId, presetName)

    override fun bind(db: StationsDatabase, prefs: Preferences): Completable {
        val dao = db.stationDao()
        return dao.getStation(stationId)
                .flatMap { station ->
                    dao.getGroup(station.groupId).map { group ->
                        db.runInTransaction {
                            dao.updateStation(station.copy(equalizerPreset = null))
                            dao.updateGroup(group.copy(equalizerPreset = presetName))
                        }
                    }
                }.ignoreElement()
    }

    override fun toString(): String {
        return "GroupPresetBinder(presetName='$presetName')"
    }
}

class GlobalPresetBinder(stationId: String, isFavorite: Boolean, presetName: String)
    : PresetBinder(stationId, isFavorite, presetName) {

    override val iconResId = R.drawable.ic_globe
    override val descriptionResId = R.string.preset_bind_all

    override fun nextBinder(): PresetBinder {
        return if (isFavorite) GroupPresetBinder(stationId, presetName)
        else this
    }

    override fun bind(db: StationsDatabase, prefs: Preferences): Completable {
        val update = if (isFavorite) {
            val dao = db.stationDao()
            dao.getStation(stationId)
                    .flatMap { station ->
                        dao.getGroup(station.groupId).map { group ->
                            db.runInTransaction {
                                dao.updateStation(station.copy(equalizerPreset = null))
                                dao.updateGroup(group.copy(equalizerPreset = null))
                            }
                        }
                    }.ignoreElement()
        } else Completable.complete()

        return update.doOnComplete { prefs.globalPreset = presetName }
    }

    override fun toString(): String {
        return "GlobalPresetBinder(presetName='$presetName')"
    }

}