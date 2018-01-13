package io.github.vladimirmi.radius.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.repository.StationListRepository
import io.github.vladimirmi.radius.model.service.PlayerService
import io.github.vladimirmi.radius.presentation.root.RootActivity
import io.reactivex.android.schedulers.AndroidSchedulers
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

        val rootIntent = Intent(this, RootActivity::class.java).apply {
            if (intent?.hasExtra(PlayerService.EXTRA_STATION_ID) == true) {
                putExtra(PlayerService.EXTRA_STATION_ID, intent.getStringExtra(PlayerService.EXTRA_STATION_ID))
            }
        }

        RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .filter { it }
                .doOnNext { Scopes.app.getInstance(StationListRepository::class.java).initStations() }
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    startActivity(rootIntent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }.addTo(compDisp)
    }

    override fun onStop() {
        super.onStop()
        compDisp.dispose()
    }
}
