package io.github.vladimirmi.radius.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.repository.StationListRepository
import io.github.vladimirmi.radius.presentation.root.RootActivity

/**
 * Created by Vladimir Mikhalev 22.12.2017.
 */

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if (granted) {
                        Scopes.app.getInstance(StationListRepository::class.java).initStations()
                        startActivity(Intent(this, RootActivity::class.java))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                }
    }
}
