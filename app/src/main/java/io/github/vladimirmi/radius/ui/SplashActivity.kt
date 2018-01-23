package io.github.vladimirmi.radius.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
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

        val interactor = Scopes.app.getInstance(StationInteractor::class.java)

        RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .filter { it }
                .flatMapCompletable { interactor.initStations() }
                .delay(500, TimeUnit.MILLISECONDS)
                .ioToMain()
                .subscribe {
                    startActivity(Intent(this, RootActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }.addTo(compDisp)
    }

    override fun onStop() {
        super.onStop()
        compDisp.dispose()
    }
}
