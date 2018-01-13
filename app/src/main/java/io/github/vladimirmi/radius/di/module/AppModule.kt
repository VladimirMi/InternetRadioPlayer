package io.github.vladimirmi.radius.di.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.vladimirmi.radius.model.interactor.PlayerControlsInteractor
import io.github.vladimirmi.radius.model.interactor.StationInteractor
import io.github.vladimirmi.radius.model.manager.ShortcutHelper
import io.github.vladimirmi.radius.model.manager.StationParser
import io.github.vladimirmi.radius.model.repository.StationIconRepository
import io.github.vladimirmi.radius.model.repository.StationListRepository
import io.github.vladimirmi.radius.model.source.StationIconSource
import io.github.vladimirmi.radius.model.source.StationSource
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

        bind(PlayerControlsInteractor::class.java).singletonInScope()
    }
}