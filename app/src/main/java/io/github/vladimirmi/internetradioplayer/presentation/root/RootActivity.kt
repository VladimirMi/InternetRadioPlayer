package io.github.vladimirmi.internetradioplayer.presentation.root

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
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
import kotlinx.android.synthetic.main.view_toolbar.*
import ru.terrakok.cicerone.NavigatorHolder
import timber.log.Timber
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
        setupDrawer()
        setupToolbar()
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

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.desc_expand_collapse,
                R.string.desc_favorite)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onStart() {
        navigatorHolder.setNavigator(navigator)
        navigator.navigationIdListener = {
            navigationView.setCheckedItem(it)
            showDirectory(it == R.id.nav_search)
        }
        super.onStart()
    }

    override fun onStop() {
        navigator.navigationIdListener = null
        navigatorHolder.removeNavigator()
        super.onStop()
    }

    override fun onNewIntent(intent: Intent?) {
        Timber.e("onNewIntent: $intent")
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
        val startPlay = intent.getBooleanExtra(EXTRA_PLAY, false)
        if (intent.hasExtra(PlayerService.EXTRA_STATION_ID)) {
            //todo legacy
            presenter.showStation(intent.getStringExtra(PlayerService.EXTRA_STATION_ID), startPlay)
        } else {
            intent.data?.let { addStation(it, startPlay) }
        }
        intent = null
    }

    override fun showMessage(resId: Int) {
        Snackbar.make(activityView, resId, Snackbar.LENGTH_LONG).show()
    }

    override fun showLoadingIndicator(visible: Boolean) {
        loadingPb.visible(visible)
    }

    fun addStation(uri: Uri, startPlay: Boolean = false) {
        presenter.addStation(uri, startPlay)
    }

    //endregion

    private fun showDirectory(show: Boolean) {
        directoryLogoIv.visible(show)
    }
}
