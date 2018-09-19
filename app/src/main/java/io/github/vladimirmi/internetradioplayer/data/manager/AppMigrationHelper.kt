package io.github.vladimirmi.internetradioplayer.data.manager

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Icon
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.StationListRepository
import io.github.vladimirmi.internetradioplayer.model.manager.decode
import io.reactivex.Completable
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.09.2018.
 */

class AppMigrationHelper
@Inject constructor(context: Context, private val gson: Gson, private val repository: StationListRepository) {

    private val appDir = context.getExternalFilesDir(null)
    private val groups = arrayListOf<Group>()
    private val stations = arrayListOf<Station>()

    fun tryMigrate(): Completable {
        val paths: Array<String> = appDir.list { _, name -> name.endsWith(".json") }
        if (paths.isEmpty()) return Completable.complete()

        return Completable.fromCallable {
            paths.forEach { path ->
                getLegacyStation(path)?.let {
                    val group = createGroup(it.group)
                    createStation(it, group)
                }
            }
        }
                .andThen(Completable.defer { Completable.merge(groups.map { repository.addGroup(it) }) })
                .andThen(Completable.defer { Completable.merge(stations.map { repository.addStation(it) }) })
                .doOnComplete { appDir.listFiles().forEach { if (it.isFile) it.delete() } }
    }

    private fun createGroup(name: String): Group {
        var group = groups.find { it.name == name }
        if (group != null) return group
        group = if (name.isBlank()) Group.default() else Group(name, groups.size)
        groups.add(group)
        return group
    }

    private fun createStation(legacyStation: LegacyStation, group: Group) {
        with(legacyStation) {
            val station = Station(
                    id, name, uri,
                    if (url.isBlank()) null else url,
                    if (bitrate == 0) null else bitrate,
                    if (sample == 0) null else sample,
                    group.stations.size,
                    getIcon("$name.png"),
                    group.id
            )
            station.genres = genre.asSequence().toSet().toList()
            group.stations.add(station)
            stations.add(station)
        }
    }


    private fun getLegacyStation(path: String): LegacyStation? {
        val file = File(appDir, path)
        return try {
            gson.fromJson(file.readText(), LegacyStation::class.java)
        } catch (e: JsonSyntaxException) {
            Timber.e(e)
            null
        }
    }

    private fun getIcon(path: String): Icon {
        val file = File(appDir, path)
        if (!file.exists()) return Icon.randomIcon()
        return file.decode()
    }

    class LegacyStation(val id: String,
                        val uri: String,
                        val name: String,
                        val group: String = Group.DEFAULT_NAME,
                        val genre: List<String> = emptyList(),
                        val url: String = "",
                        val bitrate: Int = 0,
                        val sample: Int = 0)
}
