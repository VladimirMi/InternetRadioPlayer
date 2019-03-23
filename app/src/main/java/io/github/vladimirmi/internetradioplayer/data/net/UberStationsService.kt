package io.github.vladimirmi.internetradioplayer.data.net

import io.github.vladimirmi.internetradioplayer.data.net.model.StationIdSearch
import io.github.vladimirmi.internetradioplayer.data.net.model.StationsSearch
import io.github.vladimirmi.internetradioplayer.data.net.model.SuggestionsSearch
import io.github.vladimirmi.internetradioplayer.data.net.model.TopSongsSearch
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
        const val STATIONS_ENDPOINT = "playlist.php"
        const val TOPSONGS_ENDPOINT = "topsongs.php"
    }

    @GET("presearch.php")
    fun getSuggestions(@Query("q") query: String): Single<SuggestionsSearch>


    @GET(STATIONS_ENDPOINT)
    fun searchStations(@Query("q") query: String,
                       @Query("pagesize") pageSize: Int = 50): Single<StationsSearch>

    @GET("darstations.php")
    fun getStation(@Query("station_id") id: Int): Single<StationIdSearch>

    @GET(TOPSONGS_ENDPOINT)
    fun searchTopSongs(@Query("q") query: String,
                       @Query("pagesize") pageSize: Int = 50): Single<TopSongsSearch>
}
