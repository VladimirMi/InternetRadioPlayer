package io.github.vladimirmi.radius.di.module

import io.github.vladimirmi.radius.model.repository.MediaBrowserController
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.presentation.root.RootPresenter
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import toothpick.config.Module

/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

class RootActivityModule : Module() {
    init {
        val cicerone = Cicerone.create(Router())
        bind(Router::class.java).toInstance(cicerone.router)

        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)

        bind(MediaBrowserController::class.java).singletonInScope()

        bind(RootPresenter::class.java).singletonInScope()
    }
}