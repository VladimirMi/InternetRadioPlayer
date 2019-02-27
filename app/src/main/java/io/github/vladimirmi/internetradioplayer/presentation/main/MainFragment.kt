package io.github.vladimirmi.internetradioplayer.presentation.main

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.utils.MAIN_PAGE_ID_KEY
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.isVisible
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.player.PlayerFragment
import kotlinx.android.synthetic.main.fragment_main.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class MainFragment : BaseFragment<MainPresenter, MainView>(), MainView {

    override val layout = R.layout.fragment_main

    private val playerView: ConstraintLayout by lazy {
        view!!.findViewById<ConstraintLayout>(R.id.playerFragment)
    }
    private val playerFragment: PlayerFragment by lazy {
        childFragmentManager.findFragmentById(R.id.playerFragment) as PlayerFragment
    }

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
                playerFragment.setState(slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }
        })
        playerFragment.setState(PlayerFragment.STATE_INIT)
    }

    //region =============== MainView ==============

    override fun setPageId(pageId: Int) {
        arguments = Bundle().apply { putInt(MAIN_PAGE_ID_KEY, pageId) }
        val page = when (pageId) {
            R.id.nav_search -> PAGE_SEARCH
            R.id.nav_favorites -> PAGE_FAVORITES
            else -> PAGE_HISTORY
        }
        mainPager.setCurrentItem(page, false)
        if (page == PAGE_FAVORITES) {

        }
    }

    override fun showPlayerView(visible: Boolean) {
        if (playerView.isVisible == visible) return
        playerView.visible(visible)
        if (visible) playerFragment.setState(PlayerFragment.STATE_INIT)
    }

    //endregion

    private fun openAddStationDialog() {
        NewStationDialog().show(childFragmentManager, "new_station_dialog")
    }
}
