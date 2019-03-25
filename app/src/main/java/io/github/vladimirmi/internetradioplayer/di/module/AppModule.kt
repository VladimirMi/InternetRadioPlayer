package io.github.vladimirmi.internetradioplayer.di.module

import android.content.Context
import android.media.MediaMetadataRetriever
import com.google.gson.Gson
import io.github.vladimirmi.internetradioplayer.data.db.EqualizerDatabase
import io.github.vladimirmi.internetradioplayer.data.db.HistoryDatabase
import io.github.vladimirmi.internetradioplayer.data.db.StationsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.SuggestionsDatabase
import io.github.vladimirmi.internetradioplayer.data.net.CoverArtService
import io.github.vladimirmi.internetradioplayer.data.net.RestServiceProvider
import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import io.github.vladimirmi.internetradioplayer.data.repository.*
import io.github.vladimirmi.internetradioplayer.data.service.player.LoadControl
import io.github.vladimirmi.internetradioplayer.data.utils.ShortcutHelper
import io.github.vladimirmi.internetradioplayer.data.utils.StationParser
import io.github.vladimirmi.internetradioplayer.domain.interactor.*
import okhttp3.OkHttpClient
import toothpick.config.Module

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class AppModule(context: Context) : Module() {

    init {
        bind(Context::class.java).toInstance(context)

        bind(Gson::class.java).toInstance(RestServiceProvider.gson)
        bind(OkHttpClient::class.java).toInstance(RestServiceProvider.okHttpClient)
        bind(UberStationsService::class.java).toInstance(RestServiceProvider.getUberStationsService())
        bind(CoverArtService::class.java).toInstance(RestServiceProvider.getCoverArtService())

        bind(StationsDatabase::class.java).toInstance(StationsDatabase.newInstance(context))
        bind(SuggestionsDatabase::class.java).toInstance(SuggestionsDatabase.newInstance(context))
        bind(HistoryDatabase::class.java).toInstance(HistoryDatabase.newInstance(context))
        bind(EqualizerDatabase::class.java).toInstance(EqualizerDatabase.newInstance(context))

        bind(StationParser::class.java).singletonInScope()
        bind(ShortcutHelper::class.java).singletonInScope()
        bind(MediaMetadataRetriever::class.java).toInstance(MediaMetadataRetriever())

        bind(SearchRepository::class.java).singletonInScope()
        bind(FavoritesRepository::class.java).singletonInScope()
        bind(StationRepository::class.java).singletonInScope()
        bind(PlayerRepository::class.java).singletonInScope()
        bind(HistoryRepository::class.java).singletonInScope()
        bind(EqualizerRepository::class.java).singletonInScope()
        bind(MediaRepository::class.java).singletonInScope()
        bind(RecordsRepository::class.java).singletonInScope()

        bind(MainInteractor::class.java).singletonInScope()
        bind(SearchInteractor::class.java).singletonInScope()
        bind(FavoriteListInteractor::class.java).singletonInScope()
        bind(StationInteractor::class.java).singletonInScope()
        bind(PlayerInteractor::class.java).singletonInScope()
        bind(HistoryInteractor::class.java).singletonInScope()
        bind(EqualizerInteractor::class.java).singletonInScope()
        bind(MediaInteractor::class.java).singletonInScope()
        bind(RecordsInteractor::class.java).singletonInScope()

        bind(LoadControl::class.java).singletonInScope()
    }
}
