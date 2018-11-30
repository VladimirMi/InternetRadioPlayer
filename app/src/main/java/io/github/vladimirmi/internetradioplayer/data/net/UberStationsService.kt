package io.github.vladimirmi.internetradioplayer.data.net

import io.github.vladimirmi.internetradioplayer.data.net.model.SearchResult
import io.github.vladimirmi.internetradioplayer.data.net.model.StationRes
import io.github.vladimirmi.internetradioplayer.data.net.model.SuggestionsResult
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Vladimir Mikhalev 13.11.2018.
 */

interface UberStationsService {

    @GET("presearch.php")
    fun getSuggestions(@Query("q") query: String): Single<SuggestionsResult>


    @GET("playlist.php")
    fun searchStations(@Query("q") query: String): Single<SearchResult>

    @GET("darstations.php")
    fun getStation(@Query("station_id") id: Int): Single<StationRes>
}
