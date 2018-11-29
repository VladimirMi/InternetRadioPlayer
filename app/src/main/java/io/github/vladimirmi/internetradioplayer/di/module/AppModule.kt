package io.github.vladimirmi.internetradioplayer.di.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.vladimirmi.internetradioplayer.data.db.StationsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.SuggestionsDatabase
import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import io.github.vladimirmi.internetradioplayer.data.net.createClient
import io.github.vladimirmi.internetradioplayer.data.net.getUberStationsService
import io.github.vladimirmi.internetradioplayer.data.repository.GroupListRepository
import io.github.vladimirmi.internetradioplayer.data.repository.PlayerRepository
import io.github.vladimirmi.internetradioplayer.data.repository.SearchRepository
import io.github.vladimirmi.internetradioplayer.data.repository.StationRepository
import io.github.vladimirmi.internetradioplayer.data.service.LoadControl
import io.github.vladimirmi.internetradioplayer.data.source.StationSource
import io.github.vladimirmi.internetradioplayer.data.utils.ShortcutHelper
import io.github.vladimirmi.internetradioplayer.data.utils.StationParser
import io.github.vladimirmi.internetradioplayer.domain.interactor.*
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import toothpick.config.Module

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class AppModule(context: Context) : Module() {

    init {
        bind(Context::class.java).toInstance(context)

        bind(OkHttpClient::class.java).toInstance(OkHttpClient())

        val gson = GsonBuilder().create()
        val gsonConverterFactory = GsonConverterFactory.create(gson)
        val httpClient = OkHttpClient.Builder().createClient()

        bind(Gson::class.java).toInstance(gson)
        bind(OkHttpClient::class.java).toInstance(httpClient)
        bind(UberStationsService::class.java)
                .toInstance(httpClient.getUberStationsService(gsonConverterFactory))

        val stationsDatabase = StationsDatabase.newInstance(context)
        bind(StationsDatabase::class.java).toInstance(stationsDatabase)

        val suggestionsDatabase = SuggestionsDatabase.newInstance(context)
        bind(SuggestionsDatabase::class.java).toInstance(suggestionsDatabase)

        bind(StationParser::class.java).singletonInScope()

        bind(ShortcutHelper::class.java).singletonInScope()

        bind(StationSource::class.java).singletonInScope()

        bind(SearchRepository::class.java).singletonInScope()
        bind(GroupListRepository::class.java).singletonInScope()
        bind(StationRepository::class.java).singletonInScope()
        bind(PlayerRepository::class.java).singletonInScope()

        bind(MainInteractor::class.java).singletonInScope()
        bind(SearchInteractor::class.java).singletonInScope()
        bind(FavoriteListInteractor::class.java).singletonInScope()
        bind(StationInteractor::class.java).singletonInScope()
        bind(PlayerInteractor::class.java).singletonInScope()

        bind(LoadControl::class.java).singletonInScope()
    }
}
