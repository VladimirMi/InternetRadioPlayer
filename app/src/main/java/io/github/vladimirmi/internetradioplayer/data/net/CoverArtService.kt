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
        const val HOST = "www.musicbrainz.org"
        const val BASE_URL = "http://$HOST"
        const val QUALITY_LOW = "250"
        const val QUALITY_MID = "500"
        const val QUALITY_HIGH = "1200"
    }

    @GET("ws/2/recording")
    fun searchRecordings(@Query("query") query: String,
                         @Query("limit") limit: Int = 1,
                         @Query("fmt") format: String = "json"): Single<SearchRecordingsMbid>

    fun getCoverArtUri(mbId: String, quality: String = QUALITY_LOW): String {
        return "http://coverartarchive.org/release-group/$mbId/front-$quality"
    }
}