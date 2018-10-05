package io.github.vladimirmi.internetradioplayer.data.manager

import android.content.Context
import android.net.Uri
import android.support.v4.content.FileProvider
import android.util.Xml
import io.github.vladimirmi.internetradioplayer.BuildConfig
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Icon
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.StationListRepository
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.clear
import io.reactivex.Completable
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.io.StringWriter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.10.2018.
 */

const val BACKUP_VERSION = 1

class BackupRestoreHelper
@Inject constructor(private val interactor: StationInteractor,
                    private val repository: StationListRepository,
                    private val context: Context) {

    private val ns: String? = null

    fun createBackup(): Uri {
        val file = File(context.cacheDir, "stations_backup.xml")
        if (file.exists()) file.clear()
        file.writeText(createXml(interactor.groups))

        return FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
    }

    private fun createXml(groups: List<Group>): String {
        val serializer = Xml.newSerializer()
        val writer = StringWriter()
        serializer.setOutput(writer)

        serializer.startDocument("UTF-8", true)
        serializer.startTag(ns, "data")
        serializer.attribute(ns, "version", BACKUP_VERSION.toString())

        writeStations(serializer, groups)
        writeGroups(serializer, groups)

        serializer.endTag(ns, "data")
        serializer.endDocument()

        return writer.toString()
    }

    private fun writeStations(serializer: XmlSerializer, groups: List<Group>) {
        fun writeStation(station: Station, group: String) {
            serializer.startTag(ns, "station")
            with(station) {
                serializer.attribute(ns, "id", id)
                serializer.attribute(ns, "name", name)
                serializer.attribute(ns, "group", group)
                serializer.attribute(ns, "streamUri", uri)
                url?.let { serializer.attribute(ns, "url", url) }
                bitrate?.let { serializer.attribute(ns, "bitrate", bitrate.toString()) }
                sample?.let { serializer.attribute(ns, "sample", sample.toString()) }
                serializer.attribute(ns, "genres", genres.joinToString())
                serializer.attribute(ns, "order", order.toString())
                serializer.attribute(ns, "icon_res", icon.res.toString())
                serializer.attribute(ns, "icon_bg", icon.bg.toString())
                serializer.attribute(ns, "icon_fg", icon.fg.toString())
            }
            serializer.endTag(ns, "station")
        }

        serializer.startTag(ns, "stations")
        groups.forEach { group -> group.stations.forEach { writeStation(it, group.name) } }
        serializer.endTag(ns, "stations")
    }

    private fun writeGroups(serializer: XmlSerializer, groups: List<Group>) {
        serializer.startTag(ns, "groups")
        groups.forEach { group ->
            serializer.startTag(ns, "group")
            with(group) {
                serializer.attribute(ns, "id", id)
                serializer.attribute(ns, "name", name)
                serializer.attribute(ns, "order", order.toString())
                serializer.attribute(ns, "expanded", expanded.toString())
            }
            serializer.endTag(ns, "group")
        }
        serializer.endTag(ns, "groups")
    }

    fun restoreBackup(inS: InputStream): Completable {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        val stations = arrayListOf<Station>()
        val groups = arrayListOf<Group>()
        val parse = Completable.fromCallable {
            inS.use {
                parser.setInput(it, null)
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    Timber.e("restoreBackup: ${parser.eventType} ${parser.name}")

                    if (parser.eventType == XmlPullParser.START_TAG) {
                        if (parser.name == "stations") stations.addAll(parseStations(parser))
                        if (parser.name == "groups") groups.addAll(parseGroups(parser))
                    }
                }
            }
        }
        return parse
                .andThen(Completable.defer {
                    Timber.e("restoreBackup: groups ${groups.size}")
                    Completable.merge(groups.map { repository.addGroup(it) })
                })
                .andThen(Completable.defer {
                    Timber.e("restoreBackup: stations ${stations.size}")
                    Completable.merge(stations.map { repository.addStation(it) })
                })
    }

    private fun parseStations(parser: XmlPullParser): List<Station> {
        Timber.e("parseStations: ${parser.eventType} ${parser.name}")
        val list = arrayListOf<Station>()
        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == "stations")) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "station") {
                val station = Station(
                        id = parser.getAttributeValue(ns, "id"),
                        groupId = Group.DEFAULT_ID,
                        name = parser.getAttributeValue(ns, "name"),
                        uri = parser.getAttributeValue(ns, "streamUri"),
                        url = parser.getAttributeValue(ns, "url"),
                        bitrate = parser.getAttributeValue(ns, "bitrate")?.toInt(),
                        sample = parser.getAttributeValue(ns, "sample")?.toInt(),
                        order = parser.getAttributeValue(ns, "order").toInt(),
                        icon = Icon(
                                res = parser.getAttributeValue(ns, "icon_res").toInt(),
                                bg = parser.getAttributeValue(ns, "icon_bg").toInt(),
                                fg = parser.getAttributeValue(ns, "icon_fg").toInt()
                        )
                )
                station.genres = parser.getAttributeValue(ns, "genres").split(',').map { it.trim() }
                station.groupName = parser.getAttributeValue(ns, "group")
                list.add(station)
            }
        }
        return list
    }

    private fun parseGroups(parser: XmlPullParser): List<Group> {
        Timber.e("parseGroups: ")
        val list = arrayListOf<Group>()
        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == "groups")) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "group") {
                val name = parser.getAttributeValue(ns, "name")
                if (name == Group.DEFAULT_NAME) {
                    list.add(Group.default())
                    continue
                }
                val id = parser.getAttributeValue(ns, "id")
                val expanded = parser.getAttributeValue(ns, "expanded")!!.toBoolean()
                val order = parser.getAttributeValue(ns, "order").toInt()
                val group = Group(id, name, expanded, order)
                list.add(group)
            }
        }
        return list
    }
}

