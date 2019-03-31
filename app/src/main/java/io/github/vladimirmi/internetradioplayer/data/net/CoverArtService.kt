package io.github.vladimirmi.internetradioplayer.data.net

import io.github.vladimirmi.internetradioplayer.data.net.covermodel.SearchRecordingsMbid
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Vladimir Mikhalev 25.03.2019.
 */

interface CoverArtService {

    companion object {
        const val HOST = "musicbrainz.org"
        const val BASE_URL = "http://$HOST"
        const val RECORDING_ENDPOINT = "ws/2/recording"
        const val QUALITY_LOW = "250"
        const val QUALITY_MID = "500"
        const val QUALITY_HIGH = "1200"
        const val FIELD_ARTIST = "artist"
        const val FIELD_TITLE = "recording"

        fun getCoverArtUri(mbId: String, quality: String): String {
            return "http://coverartarchive.org/release-group/$mbId/front-$quality"
        }
    }

    @GET(RECORDING_ENDPOINT)
    fun searchRecordings(@Query("query") query: String,
                         @Query("limit") limit: Int = 1,
                         @Query("fmt") format: String = "json"): Single<SearchRecordingsMbid>
}