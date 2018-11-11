package io.github.vladimirmi.internetradioplayer.data.utils

import android.content.Context
import android.net.Uri
import android.util.Xml
import androidx.core.content.FileProvider
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
import java.io.File
import java.io.InputStream
import java.io.StringWriter
import java.util.*
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.10.2018.
 */

const val BACKUP_TYPE = "text/xml"
private const val BACKUP_NAME = "stations_backup.xml"
private const val BACKUP_ENCODING = "UTF-8"
private const val BACKUP_VERSION = 1

private const val DATA_TAG = "data"
private const val STATIONS_TAG = "stations"
private const val STATION_TAG = "station"
private const val GROUPS_TAG = "groups"
private const val GROUP_TAG = "group"

private const val VERSION_ATTR = "version"
private const val NAME_ATTR = "name"
private const val GROUP_ATTR = "group"
private const val URI_ATTR = "streamUri"
private const val URL_ATTR = "url"
private const val BITRATE_ATTR = "bitrate"
private const val SAMPLE_ATTR = "sample"
private const val ICON_RES_ATTR = "icon_res"
private const val ICON_BG_ATTR = "icon_bg"
private const val ICON_FG_ATTR = "icon_fg"
private const val GENRES_ATTR = "genres"
private const val ORDER_ATTR = "order"
private const val EXPANDED_ATTR = "expanded"

class BackupRestoreHelper
@Inject constructor(private val interactor: StationInteractor,
                    private val repository: StationListRepository,
                    private val context: Context) {

    private val ns: String? = null

    fun createBackup(): Uri {
        val file = File(context.cacheDir, BACKUP_NAME)
        if (file.exists()) file.clear()
        file.writeText(createXml(interactor.groups))

        return FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
    }

    private fun createXml(groups: List<Group>): String {
        val serializer = Xml.newSerializer()
        val writer = StringWriter()
        serializer.setOutput(writer)

        serializer.startDocument(BACKUP_ENCODING, null)
        serializer.startTag(ns, DATA_TAG)
        serializer.attribute(ns, VERSION_ATTR, BACKUP_VERSION.toString())

        writeStations(serializer, groups)
        writeGroups(serializer, groups)

        serializer.endTag(ns, DATA_TAG)
        serializer.endDocument()

        return writer.toString()
    }

    private fun writeStations(serializer: XmlSerializer, groups: List<Group>) {
        fun writeStation(station: Station, group: String) {
            serializer.startTag(ns, STATION_TAG)
            with(station) {
                serializer.attribute(ns, NAME_ATTR, name)
                serializer.attribute(ns, GROUP_ATTR, group)
                serializer.attribute(ns, URI_ATTR, uri)
                url?.let { serializer.attribute(ns, URL_ATTR, url) }
                bitrate?.let { serializer.attribute(ns, BITRATE_ATTR, bitrate.toString()) }
                sample?.let { serializer.attribute(ns, SAMPLE_ATTR, sample.toString()) }
                serializer.attribute(ns, GENRES_ATTR, genres.joinToString())
                serializer.attribute(ns, ORDER_ATTR, order.toString())
                serializer.attribute(ns, ICON_RES_ATTR, icon.res.toString())
                serializer.attribute(ns, ICON_BG_ATTR, icon.bg.toString())
                serializer.attribute(ns, ICON_FG_ATTR, icon.fg.toString())
            }
            serializer.endTag(ns, STATION_TAG)
        }

        serializer.startTag(ns, STATIONS_TAG)
        groups.forEach { group -> group.stations.forEach { writeStation(it, group.name) } }
        serializer.endTag(ns, STATIONS_TAG)
    }

    private fun writeGroups(serializer: XmlSerializer, groups: List<Group>) {
        serializer.startTag(ns, GROUPS_TAG)
        groups.forEach { group ->
            serializer.startTag(ns, GROUP_TAG)
            with(group) {
                serializer.attribute(ns, NAME_ATTR, name)
                serializer.attribute(ns, ORDER_ATTR, order.toString())
                serializer.attribute(ns, EXPANDED_ATTR, expanded.toString())
            }
            serializer.endTag(ns, GROUP_TAG)
        }
        serializer.endTag(ns, GROUPS_TAG)
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

                    if (parser.eventType == XmlPullParser.START_TAG) {
                        if (parser.name == STATIONS_TAG) stations.addAll(parseStations(parser))
                        if (parser.name == GROUPS_TAG) groups.addAll(parseGroups(parser))
                    }
                }
            }
        }
        return parse
                .andThen(Completable.defer {
                    Completable.merge(groups.map { repository.addGroup(it) })
                })
                .andThen(Completable.defer {
                    Completable.merge(stations.map { repository.addStation(it) })
                })
    }

    private fun parseStations(parser: XmlPullParser): List<Station> {
        val list = arrayListOf<Station>()
        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == STATIONS_TAG)) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == STATION_TAG) {
                val station = Station(
                        id = UUID.randomUUID().toString(),
                        groupId = Group.DEFAULT_ID,
                        name = parser.getAttributeValue(ns, NAME_ATTR),
                        uri = parser.getAttributeValue(ns, URI_ATTR),
                        url = parser.getAttributeValue(ns, URL_ATTR),
                        bitrate = parser.getAttributeValue(ns, BITRATE_ATTR)?.toInt(),
                        sample = parser.getAttributeValue(ns, SAMPLE_ATTR)?.toInt(),
                        order = parser.getAttributeValue(ns, ORDER_ATTR).toInt(),
                        icon = Icon(
                                res = parser.getAttributeValue(ns, ICON_RES_ATTR).toInt(),
                                bg = parser.getAttributeValue(ns, ICON_BG_ATTR).toInt(),
                                fg = parser.getAttributeValue(ns, ICON_FG_ATTR).toInt()
                        )
                )
                station.genres = parser.getAttributeValue(ns, GENRES_ATTR)
                        .split(',').map { it.trim() }
                station.groupName = parser.getAttributeValue(ns, GROUP_ATTR)
                list.add(station)
            }
        }
        return list
    }

    private fun parseGroups(parser: XmlPullParser): List<Group> {
        val list = arrayListOf<Group>()
        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == GROUPS_TAG)) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == GROUP_TAG) {
                val name = parser.getAttributeValue(ns, NAME_ATTR)
                if (name == Group.DEFAULT_NAME) {
                    list.add(Group.default())
                    continue
                }
                val id = UUID.randomUUID().toString()
                val expanded = parser.getAttributeValue(ns, EXPANDED_ATTR)!!.toBoolean()
                val order = parser.getAttributeValue(ns, ORDER_ATTR).toInt()
                val group = Group(id, name, expanded, order)
                list.add(group)
            }
        }
        return list
    }
}

