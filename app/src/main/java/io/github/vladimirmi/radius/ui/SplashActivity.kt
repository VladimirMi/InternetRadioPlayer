package io.github.vladimirmi.radius.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import io.github.vladimirmi.radius.presentation.root.RootActivity

/**
 * Created by Vladimir Mikhalev 22.12.2017.
 */

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //todo init stations
        Handler().postDelayed({
            startActivity(Intent(this, RootActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2000)
    }
}
