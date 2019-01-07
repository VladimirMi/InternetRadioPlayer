package io.github.vladimirmi.internetradioplayer

import android.app.Application
import com.facebook.stetho.Stetho
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.di.module.AppModule
import io.github.vladimirmi.internetradioplayer.extensions.FileLoggingTree
import io.github.vladimirmi.internetradioplayer.extensions.globalErrorHandler
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.registries.FactoryRegistryLocator.setRootRegistry
import toothpick.registries.MemberInjectorRegistryLocator.setRootRegistry


/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

@Suppress("unused")
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Toothpick.setConfiguration(Configuration.forDevelopment().preventMultipleRootScopes())
        } else {
            Toothpick.setConfiguration(Configuration.forProduction().disableReflection())
            setRootRegistry(FactoryRegistry())
            setRootRegistry(MemberInjectorRegistry())
        }

        Scopes.app.installModules(AppModule(this))

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
            Timber.plant(FileLoggingTree.Builder(Scopes.context)
                    .log(FileLoggingTree.Logs.ERROR)
                    .build()
            )
        }

        RxJavaPlugins.setErrorHandler(globalErrorHandler)
    }
}
