package io.github.vladimirmi.internetradioplayer.data.net

import io.github.vladimirmi.internetradioplayer.data.net.ubermodel.*
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Vladimir Mikhalev 13.11.2018.
 */

interface UberStationsService {

    companion object {
        const val HOST = "api.dar.fm"
        const val BASE_URL = "http://$HOST"
        const val PRESEARCH_ENDPOINT = "presearch.php"
        const val STATIONS_ENDPOINT = "playlist.php"
        const val STATION_ENDPOINT = "darstations.php"
        const val TOPSONGS_ENDPOINT = "topsongs.php"
        const val TALKS_ENDPOINT = "uberguide.php"
        const val TALK_ENDPOINT = "darstations.php"
    }

    @GET(PRESEARCH_ENDPOINT)
    fun getSuggestions(@Query("q") query: String): Single<SuggestionsSearch>


    @GET(STATIONS_ENDPOINT)
    fun searchStations(@Query("q") query: String,
                       @Query("pagesize") pageSize: Int = 50): Single<StationsSearch>

    @GET(STATION_ENDPOINT)
    fun getStation(@Query("station_id") id: Int): Single<StationIdSearch>

    @GET(TOPSONGS_ENDPOINT)
    fun searchTopSongs(@Query("q") query: String,
                       @Query("pagesize") pageSize: Int = 50): Single<TopSongsSearch>

    @GET(TALKS_ENDPOINT)
    fun getTalks(): Single<TalksSearch>

    @GET(TALK_ENDPOINT)
    fun searchTalk(@Query("showinfo_id") id: String): Single<TalkIdSearch>
}
