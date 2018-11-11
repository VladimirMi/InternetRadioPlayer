package io.github.vladimirmi.internetradioplayer.presentation.root

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.Visibility
import com.google.android.material.snackbar.Snackbar
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import io.github.vladimirmi.internetradioplayer.data.utils.EXTRA_PLAY
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.di.module.RootActivityModule
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.navigation.Navigator
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseActivity
import kotlinx.android.synthetic.main.activity_root.*
import ru.terrakok.cicerone.NavigatorHolder
import toothpick.Toothpick
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

class RootActivity : BaseActivity<RootPresenter, RootView>(), RootView {

    @Inject lateinit var navigatorHolder: NavigatorHolder

    override val layout = R.layout.activity_root
    private val navigator by lazy { Navigator(this, R.id.mainFr) }

    override fun providePresenter(): RootPresenter = Scopes.rootActivity.getInstance(RootPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        Scopes.rootActivity.apply {
            installModules(RootActivityModule())
            Toothpick.inject(this@RootActivity, this)
        }
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null || (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            intent = null // stop redeliver old intent
        }
    }

    override fun setupView() {
        setSupportActionBar(toolbar)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    @SuppressLint("RestrictedApi")
    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onDestroy() {
        if (isFinishing) Toothpick.closeScope(Scopes.ROOT_ACTIVITY)
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.string.menu_settings -> presenter.openSettings()
            R.string.menu_exit -> presenter.exitApp()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    //region =============== RootView ==============

    override fun checkIntent() {
        if (intent != null) {
            val startPlay = intent.getBooleanExtra(EXTRA_PLAY, false)
            if (intent.hasExtra(PlayerService.EXTRA_STATION_ID)) {
                //todo legacy
                presenter.showStation(intent.getStringExtra(PlayerService.EXTRA_STATION_ID), startPlay)
            } else {
                intent.data?.let { addStation(it, startPlay) }
            }
            intent = null
        }
    }

    override fun showMessage(resId: Int) {
        Snackbar.make(activityView, resId, Snackbar.LENGTH_LONG).show()
    }

    override fun showControls(visible: Boolean) {
        val slide = createSlideTransition()
        slide.mode = if (visible) Visibility.MODE_IN else Visibility.MODE_OUT
        TransitionManager.beginDelayedTransition(root, slide)
        playerControlsFr.view?.visible(visible)
    }

    override fun showLoadingIndicator(visible: Boolean) {
        loadingPb.visible(visible)
    }

    fun addStation(uri: Uri, startPlay: Boolean = false) {
        presenter.addStation(uri, startPlay)
    }

    //endregion

    private fun createSlideTransition(): Slide {
        val slide = Slide()
        slide.slideEdge = Gravity.BOTTOM
        slide.duration = 300
        slide.addTarget(R.id.playerControlsFr)
        slide.interpolator = FastOutSlowInInterpolator()
        return slide
    }
}
