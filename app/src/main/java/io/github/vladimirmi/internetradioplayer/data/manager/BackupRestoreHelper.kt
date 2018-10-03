package io.github.vladimirmi.internetradioplayer.data.manager

import android.content.Context
import android.net.Uri
import android.support.v4.content.FileProvider
import android.util.Xml
import io.github.vladimirmi.internetradioplayer.BuildConfig
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.StationListRepository
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.clear
import io.reactivex.rxkotlin.Singles
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.StringWriter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.10.2018.
 */

class BackupRestoreHelper
@Inject constructor(private val interactor: StationInteractor,
                    private val context: Context) {

    fun createBackup(): Uri {
        val file = File(context.cacheDir, "backup.xml")
        if (file.exists()) file.clear()
        file.writeText(createXml(interactor.groups))

        return FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
    }

    private fun createXml(groups: List<Group>): String {
        val serializer = Xml.newSerializer()
        val writer = StringWriter()
        serializer.setOutput(writer)

        serializer.startDocument("UTF-8", true)
        serializer.startTag("", "data")
        serializer.attribute("", "version", "1")

        writeStations(serializer, groups)
        writeGroups(serializer, groups)

        serializer.endTag("", "data")
        serializer.endDocument()

        return writer.toString()
    }

    private fun writeStations(serializer: XmlSerializer, groups: List<Group>) {
        fun writeStation(station: Station, group: String) {
            serializer.startTag("", "station")
            with(station) {
                serializer.attribute("", "name", name)
                serializer.attribute("", "group", group)
                serializer.attribute("", "streamUri", uri)
                url?.let { serializer.attribute("", "url", url) }
                bitrate?.let { serializer.attribute("", "bitrate", bitrate.toString()) }
                sample?.let { serializer.attribute("", "sample", sample.toString()) }
                serializer.attribute("", "genre", genres.joinToString())
                serializer.attribute("", "order", order.toString())
            }
            serializer.endTag("", "station")
        }

        serializer.startTag("", "stations")
        groups.forEach { group -> group.stations.forEach { writeStation(it, group.name) } }
        serializer.endTag("", "stations")
    }

    private fun writeGroups(serializer: XmlSerializer, groups: List<Group>) {
        serializer.startTag("", "groups")
        groups.forEach { group ->
            serializer.startTag("", "group")
            with(group) {
                serializer.attribute("", "name", name)
                serializer.attribute("", "order", order.toString())
                serializer.attribute("", "expanded", expanded.toString())
            }
            serializer.endTag("", "group")
        }
        serializer.endTag("", "groups")
    }
}

