package io.github.vladimirmi.internetradioplayer.di.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.vladimirmi.internetradioplayer.model.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.model.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.model.manager.ShortcutHelper
import io.github.vladimirmi.internetradioplayer.model.manager.StationParser
import io.github.vladimirmi.internetradioplayer.model.repository.StationIconRepository
import io.github.vladimirmi.internetradioplayer.model.repository.StationListRepository
import io.github.vladimirmi.internetradioplayer.model.source.StationIconSource
import io.github.vladimirmi.internetradioplayer.model.source.StationSource
import toothpick.config.Module

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class AppModule(context: Context) : Module() {
    init {
        bind(Context::class.java).toInstance(context)

        val gson = GsonBuilder().setPrettyPrinting().create()
        bind(Gson::class.java).toInstance(gson)

        bind(StationParser::class.java).singletonInScope()

        bind(ShortcutHelper::class.java).singletonInScope()

        bind(StationSource::class.java).singletonInScope()

        bind(StationIconSource::class.java).singletonInScope()

        bind(StationListRepository::class.java).singletonInScope()

        bind(StationIconRepository::class.java).singletonInScope()

        bind(StationInteractor::class.java).singletonInScope()

        //todo Why MediaController in RootActivity scope
        bind(PlayerControlsInteractor::class.java).singletonInScope()
    }
}
