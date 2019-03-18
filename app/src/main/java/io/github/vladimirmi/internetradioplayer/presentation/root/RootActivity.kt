package io.github.vladimirmi.internetradioplayer.presentation.root

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import io.github.vladimirmi.internetradioplayer.data.utils.ShortcutHelper
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.di.module.RootActivityModule
import io.github.vladimirmi.internetradioplayer.extensions.lock
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.navigation.Navigator
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseActivity
import io.github.vladimirmi.internetradioplayer.presentation.player.isExpanded
import io.github.vladimirmi.internetradioplayer.presentation.player.isHidden
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.view_toolbar.*
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
    private lateinit var playerBehavior: BottomSheetBehavior<View>

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
        setupDrawer()
        setupToolbar()
        playerBehavior = BottomSheetBehavior.from(findViewById(R.id.playerFragment))
    }

    private fun setupDrawer() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerClosed(drawerView: View) {
                    presenter.navigateTo(menuItem.itemId)
                    drawerLayout.removeDrawerListener(this)
                }
            })
            true
        }
    }

    lateinit var toggle: ActionBarDrawerToggle

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.desc_open_drawer,
                R.string.desc_close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.setToolbarNavigationClickListener { onBackPressed() }
    }

    override fun onStart() {
        navigatorHolder.setNavigator(navigator)
        navigator.navigationIdListener = {
            navigationView.setCheckedItem(it)
            showDirectory(it == R.id.nav_search)
            setHomeAsUp(it == R.id.nav_settings || it == R.id.nav_equalizer)
            presenter.checkPlayerVisibility(isPlayerEnabled = it != R.id.nav_settings)
        }
        super.onStart()
    }

    override fun onStop() {
        navigator.navigationIdListener = null
        navigatorHolder.removeNavigator()
        super.onStop()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        if (isPresenterInitialized) checkIntent()
    }

    override fun onDestroy() {
        if (isFinishing) Toothpick.closeScope(Scopes.ROOT_ACTIVITY)
        super.onDestroy()
    }

    //region =============== RootView ==============

    override fun checkIntent() {
        if (intent == null) return
        val startPlay = intent.getBooleanExtra(ShortcutHelper.EXTRA_PLAY, false)
        val stationName = intent.getStringExtra(ShortcutHelper.EXTRA_STATION_NAME)
        if (intent.hasExtra(PlayerService.EXTRA_STATION_ID)) {
            presenter.showStation(intent.getStringExtra(PlayerService.EXTRA_STATION_ID), startPlay)
        } else {
            intent.data?.let {
                createStation(it, stationName, addToFavorite = false, startPlay = startPlay)
            }
        }
        intent = null
    }

    override fun showSnackbar(resId: Int) {
        Snackbar.make(activityView, resId, Snackbar.LENGTH_LONG).show()
    }

    override fun showLoadingIndicator(visible: Boolean) {
        loadingPb.visible(visible)
    }

    override fun hidePlayer() {
        playerBehavior.isHideable = true
        playerBehavior.isHidden = true
    }

    override fun collapsePlayer() {
        activityView.postDelayed({
            playerBehavior.isHideable = false
            playerBehavior.isHidden = false

        }, 300)
    }

    override fun expandPlayer() {
        playerBehavior.isHideable = false
        playerBehavior.isExpanded = true
    }

    override fun createStation(uri: Uri, name: String?, addToFavorite: Boolean, startPlay: Boolean) {
        presenter.createStation(uri, name, addToFavorite, startPlay)
    }

    override fun setOffset(offset: Float) {
        mainFr.setPadding(0, 0, 0, (resources.getDimension(R.dimen.player_collapsed_height) * offset).toInt())
    }

    //endregion

    private fun showDirectory(show: Boolean) {
        directoryLogoIv.visible(show)
    }

    private var homeAsUp = false

    private fun setHomeAsUp(isHomeAsUp: Boolean) {
        if (homeAsUp == isHomeAsUp) return
        homeAsUp = isHomeAsUp
        drawerLayout.lock(isHomeAsUp)
        val anim = if (isHomeAsUp) ValueAnimator.ofFloat(0f, 1f) else ValueAnimator.ofFloat(1f, 0f)
        anim.addUpdateListener { valueAnimator ->
            toggle.onDrawerSlide(drawerLayout, valueAnimator.animatedValue as Float)
        }
        anim.interpolator = DecelerateInterpolator()
        anim.duration = 400
        if (isHomeAsUp) {
            Handler().postDelayed({ toggle.isDrawerIndicatorEnabled = false }, 400)
        } else {
            toggle.isDrawerIndicatorEnabled = true
            toggle.onDrawerSlide(drawerLayout, 1f)
        }
        anim.start()
    }
}
