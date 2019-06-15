package io.github.vladimirmi.internetradioplayer.presentation.root

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import io.github.vladimirmi.internetradioplayer.data.utils.ShortcutHelper
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.di.module.RootActivityModule
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseActivity
import io.github.vladimirmi.internetradioplayer.presentation.navigation.drawer.DrawerFragment
import io.github.vladimirmi.internetradioplayer.presentation.player.isExpanded
import io.github.vladimirmi.internetradioplayer.presentation.player.isHidden
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.view_toolbar.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

class RootActivity : BaseActivity<RootPresenter, RootView>(), RootView {

    override val layout = R.layout.activity_root
    private lateinit var playerBehavior: BottomSheetBehavior<View>

    override fun providePresenter(): RootPresenter = Scopes.rootActivity.getInstance(RootPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        Scopes.rootActivity.apply {
            installModules(RootActivityModule())
        }
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null || (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            intent = null // stop redeliver old intent
        }
    }

    override fun setupView() {
        setupToolbar()
        playerBehavior = BottomSheetBehavior.from(findViewById(R.id.playerFragment))
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val drawerFragment = supportFragmentManager.findFragmentById(R.id.drawerFr) as DrawerFragment
        drawerFragment.init(drawerLayout, toolbar)
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
        val lp = rootContainer.layoutParams as ViewGroup.MarginLayoutParams
        lp.bottomMargin = (resources.getDimension(R.dimen.player_collapsed_height) * offset).toInt()
        rootContainer.layoutParams = lp
    }

    //endregion
}
