package io.github.vladimirmi.internetradioplayer.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.ioToMain
import io.github.vladimirmi.internetradioplayer.model.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.presentation.root.RootActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

/**
 * Created by Vladimir Mikhalev 22.12.2017.
 */

class SplashActivity : AppCompatActivity() {

    private val compDisp = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Scopes.app.getInstance(StationInteractor::class.java).initStations()
                .delay(500, TimeUnit.MILLISECONDS)
                .ioToMain()
                .subscribe { startRootActivity(Intent(this, RootActivity::class.java)) }
                .addTo(compDisp)
    }

    override fun onStop() {
        super.onStop()
        compDisp.dispose()
    }

    private fun startRootActivity(rootIntent: Intent) {
        startActivity(rootIntent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
