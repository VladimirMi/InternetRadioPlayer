package io.github.vladimirmi.internetradioplayer.data.manager

import android.content.Context
import android.net.Uri
import android.support.v4.content.FileProvider
import android.util.Xml
import io.github.vladimirmi.internetradioplayer.BuildConfig
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.clear
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.InputStream
import java.io.StringWriter
import javax.inject.Inject
import org.xmlpull.v1.XmlPullParser.FEATURE_PROCESS_NAMESPACES

/**
 * Created by Vladimir Mikhalev 02.10.2018.
 */

class BackupRestoreHelper
@Inject constructor(private val interactor: StationInteractor,
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
        serializer.attribute(ns, "version", "1")

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
                serializer.attribute(ns, "name", name)
                serializer.attribute(ns, "group", group)
                serializer.attribute(ns, "streamUri", uri)
                url?.let { serializer.attribute(ns, "url", url) }
                bitrate?.let { serializer.attribute(ns, "bitrate", bitrate.toString()) }
                sample?.let { serializer.attribute(ns, "sample", sample.toString()) }
                serializer.attribute(ns, "genre", genres.joinToString())
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
                serializer.attribute(ns, "name", name)
                serializer.attribute(ns, "order", order.toString())
                serializer.attribute(ns, "expanded", expanded.toString())
            }
            serializer.endTag(ns, "group")
        }
        serializer.endTag(ns, "groups")
    }



    fun restoreBackup(inS: InputStream) {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)

        inS.use {
            parser.setInput(it, null)
            parser.nextTag()
        }
    }
}

