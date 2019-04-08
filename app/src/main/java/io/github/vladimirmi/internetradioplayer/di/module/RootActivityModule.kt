package io.github.vladimirmi.internetradioplayer.di.module

import io.github.vladimirmi.internetradioplayer.presentation.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.root.RootPresenter
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
        bind(RootPresenter::class.java).singletonInScope()
    }
}
