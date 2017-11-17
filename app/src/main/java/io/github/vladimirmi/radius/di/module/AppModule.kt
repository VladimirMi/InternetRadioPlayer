package io.github.vladimirmi.radius.di.module

import android.content.Context
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.model.source.StationSource
import toothpick.config.Module

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class AppModule(context: Context) : Module() {
    init {
        bind(Context::class.java).toInstance(context)
        bind(StationSource::class.java).singletonInScope()
        bind(StationRepository::class.java).singletonInScope()
    }
}