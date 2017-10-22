package io.github.vladimirmi.radius.di.module

import io.github.vladimirmi.radius.data.repository.MediaBrowserController
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.config.Module

/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

class RootActivityModule : Module() {
    init {
        val cicerone = Cicerone.create()
        bind(Router::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)
        bind(MediaBrowserController::class.java).singletonInScope()
    }
}