package io.github.vladimirmi.internetradioplayer.data.net

import io.github.vladimirmi.internetradioplayer.data.net.model.PresearchRes
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Vladimir Mikhalev 13.11.2018.
 */

interface UberStationsService {

    @GET("presearch.php")
    fun getSuggestions(@Query("q") query: String): Single<PresearchRes>

}
