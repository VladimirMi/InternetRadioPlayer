package io.github.vladimirmi.radius

import android.app.Application
import com.facebook.stetho.Stetho
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.di.module.AppModule
import timber.log.Timber
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.registries.FactoryRegistryLocator
import toothpick.registries.MemberInjectorRegistryLocator

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        if (BuildConfig.DEBUG) {
            Toothpick.setConfiguration(Configuration.forDevelopment().preventMultipleRootScopes())
        } else {
            Toothpick.setConfiguration(Configuration.forProduction().disableReflection())
            FactoryRegistryLocator.setRootRegistry(io.github.vladimirmi.radius.FactoryRegistry())
            MemberInjectorRegistryLocator.setRootRegistry(io.github.vladimirmi.radius.MemberInjectorRegistry())
        }

        Scopes.app.installModules(AppModule(this))
    }
}