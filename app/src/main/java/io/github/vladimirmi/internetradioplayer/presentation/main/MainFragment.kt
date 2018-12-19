package io.github.vladimirmi.internetradioplayer.presentation.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.utils.MAIN_PAGE_ID_KEY
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.setTintExt
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.extensions.waitForMeasure
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.view_controls_simple.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class MainFragment : BaseFragment<MainPresenter, MainView>(), MainView, SimpleControlsView {

    override val layout = R.layout.fragment_main
    private var controlsVisibility = 0f

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
        mainPager.adapter = MainPagerAdapter(context!!, childFragmentManager)
        mainTl.setupWithViewPager(mainPager)
        val pageId = arguments?.getInt(MAIN_PAGE_ID_KEY) ?: 0
        setPageId(pageId)

        mainPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                presenter.selectPage(position)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val visibility = Math.min(1f, Math.abs(PAGE_PLAYER - (position + positionOffset)))
                showControls(visibility)
            }
        })

        sPlayPauseBt.setManualMode(true)
        sPlayPauseBt.setOnClickListener { presenter.playPause() }
        sMetadataTv.isSelected = true
        sBufferingPb.indeterminateDrawable.setTintExt(requireContext().color(R.color.pause_button))
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

    override fun showStopped() {
        sPlayPauseBt.setPlaying(false, controlsVisibility > 0)
        sBufferingPb.visible(false)
    }

    override fun showPlaying() {
        sPlayPauseBt.setPlaying(true, controlsVisibility > 0)
        sBufferingPb.visible(false)
    }

    override fun showBuffering() {
        sPlayPauseBt.setPlaying(true, controlsVisibility > 0)
        sBufferingPb.visible(true)
    }

    override fun setMetadata(metadata: String) {
        sMetadataTv.text = metadata
    }

    override fun showControls(show: Boolean) {
        simpleControlsContainer.visible(show)
        showControls(controlsVisibility)
    }

    override fun showControls(visibility: Float) {
        simpleControlsContainer.waitForMeasure {
            val set = ConstraintSet()
            set.clone(mainCl)
            set.setMargin(R.id.mainPager, ConstraintSet.BOTTOM,
                    (simpleControlsContainer.height * visibility).toInt())
            set.applyTo(mainCl)

            childFragmentManager.fragments
                    .filterIsInstance(SimpleControlsView::class.java)
                    .forEach { it.showControls(visibility) }

            controlsVisibility = visibility
        }
    }

    //endregion

    private fun openAddStationDialog() {
        NewStationDialog().show(childFragmentManager, "new_station_dialog")
    }
}
