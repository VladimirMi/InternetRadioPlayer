package io.github.vladimirmi.internetradioplayer.presentation.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.utils.MAIN_PAGE_ID_KEY
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView
import io.github.vladimirmi.internetradioplayer.presentation.player.PlayerViewImpl
import kotlinx.android.synthetic.main.fragment_main.*
import timber.log.Timber
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class MainFragment : BaseFragment<MainPresenter, MainView>(), MainView {

    override val layout = R.layout.fragment_main

    companion object {
        fun newInstance(page: Int): MainFragment {
            return MainFragment().apply {
                arguments = Bundle().apply { putInt(MAIN_PAGE_ID_KEY, page) }
            }
        }
    }

    override fun providePresenter(): MainPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(MainPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        setupPager()
        setupPlayerView()
    }

    private fun setupPager() {
        mainPager.adapter = MainPagerAdapter(context!!, childFragmentManager)
        mainPager.offscreenPageLimit = 3
        mainTl.setupWithViewPager(mainPager)
        val pageId = arguments?.getInt(MAIN_PAGE_ID_KEY) ?: 0
        setPageId(pageId)

        mainPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                presenter.selectPage(position)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
        })
    }

    private fun setupPlayerView() {
        BottomSheetBehavior.from(playerView).setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                (playerView as? PlayerViewImpl)?.setState(slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }
        })
        (playerView as? PlayerViewImpl)?.setState(PlayerViewImpl.STATE_INIT)
    }

    @SuppressLint("RestrictedApi")
    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        val item = menu.add(0, R.string.menu_add_station, 0, R.string.menu_add_station)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        item.icon = ContextCompat.getDrawable(context!!, R.drawable.ic_add)
        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.string.menu_add_station -> openAddStationDialog()
            else -> return false
        }
        return true
    }

    //region =============== MainView ==============

    override fun setPageId(pageId: Int) {
        arguments = Bundle().apply { putInt(MAIN_PAGE_ID_KEY, pageId) }
        val page = when (pageId) {
            R.id.nav_search -> PAGE_SEARCH
            R.id.nav_favorites -> PAGE_FAVORITES
            R.id.nav_player -> PAGE_PLAYER
            else -> PAGE_HISTORY
        }
        mainPager.setCurrentItem(page, false)
    }

    override fun showPlayerView(visible: Boolean) {
        Timber.e("showPlayerView: $visible")
        playerView.visible(visible)
        if (visible) (playerView as? PlayerViewImpl)?.setState(PlayerViewImpl.STATE_INIT)
    }

    //endregion

    override fun onStart() {
        super.onStart()
        (playerView as? BaseView)?.onStart()
    }

    override fun onStop() {
        super.onStop()
        (playerView as? BaseView)?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        (playerView as? BaseView)?.onDestroy()
    }

    private fun openAddStationDialog() {
        NewStationDialog().show(childFragmentManager, "new_station_dialog")
    }
}
