package io.github.vladimirmi.radius.ui

import android.R
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.ioToMain
import io.github.vladimirmi.radius.model.interactor.StationInteractor
import io.github.vladimirmi.radius.presentation.root.RootActivity
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
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
