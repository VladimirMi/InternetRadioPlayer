package io.github.vladimirmi.radius.di.module

import io.github.vladimirmi.radius.model.repository.MediaRepository
import toothpick.config.Module

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

class RepositoryModule : Module() {
    init {
        bind(MediaRepository::class.java).singletonInScope()
    }
}