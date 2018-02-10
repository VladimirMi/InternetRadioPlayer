package io.github.vladimirmi.internetradioplayer

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.stetho.Stetho
import io.fabric.sdk.android.Fabric
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.di.module.AppModule
import io.github.vladimirmi.internetradioplayer.extensions.CrashlyticsTree
import io.github.vladimirmi.internetradioplayer.extensions.FileLoggingTree
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

        val core = CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build()
        Fabric.with(this, Crashlytics.Builder().core(core).build())

        if (BuildConfig.DEBUG) {
            Toothpick.setConfiguration(Configuration.forDevelopment().preventMultipleRootScopes())
        } else {
            Toothpick.setConfiguration(Configuration.forProduction().disableReflection())
            FactoryRegistryLocator.setRootRegistry(io.github.vladimirmi.internetradioplayer.FactoryRegistry())
            MemberInjectorRegistryLocator.setRootRegistry(io.github.vladimirmi.internetradioplayer.MemberInjectorRegistry())
        }

        Scopes.app.installModules(AppModule(this))

        if (BuildConfig.DEBUG) {
            Timber.plant(
                    FileLoggingTree.Builder(Scopes.context)
                            .log(FileLoggingTree.Logs.ERROR)
                            .build()
            )
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }
}