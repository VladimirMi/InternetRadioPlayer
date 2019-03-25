package io.github.vladimirmi.internetradioplayer.data.net.covermodel

import com.google.gson.annotations.SerializedName


/**
 * Created by Vladimir Mikhalev 25.03.2019.
 */
class SearchRecordingsMbid(
        val recordings: List<RecordingMbId>
) {

    fun getReleaseGroupId(): String {
        return recordings.flatMap { it.releases }
                .asSequence()
                .filter { it.status == "Official" }
                .map { it.releaseGroup }
                .filter { it.primaryType == "Album" }
                .map { it.id }
                .distinct()
                .firstOrNull() ?: ""
    }
}

class RecordingMbId(
        val id: String,
        val title: String,
        val releases: List<ReleaseMbId>
)

class ReleaseMbId(
        val id: String,
        val title: String,
        val status: String,
        @SerializedName("release-group")
        val releaseGroup: ReleaseGroupMbId
)

class ReleaseGroupMbId(
        val id: String,
        @SerializedName("primary-type")
        val primaryType: String
)