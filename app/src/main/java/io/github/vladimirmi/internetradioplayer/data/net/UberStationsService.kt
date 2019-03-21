package io.github.vladimirmi.internetradioplayer.data.net

import io.github.vladimirmi.internetradioplayer.data.net.model.SearchResult
import io.github.vladimirmi.internetradioplayer.data.net.model.StationsResult
import io.github.vladimirmi.internetradioplayer.data.net.model.SuggestionsResult
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
    }

    @GET("presearch.php")
    fun getSuggestions(@Query("q") query: String): Single<SuggestionsResult>


    @GET(STATIONS_ENDPOINT)
    fun searchStations(@Query("q") query: String, @Query("pagesize") pageSize: Int = 50): Single<SearchResult>

    @GET("darstations.php")
    fun getStation(@Query("station_id") id: Int): Single<StationsResult>
}
