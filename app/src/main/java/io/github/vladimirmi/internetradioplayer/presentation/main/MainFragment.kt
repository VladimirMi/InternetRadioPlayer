package io.github.vladimirmi.internetradioplayer.presentation.main

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.Visibility
import androidx.viewpager.widget.ViewPager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.utils.MAIN_PAGE_ID_KEY
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_main.*
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
        mainPager.adapter = MainPagerAdapter(context!!, childFragmentManager)
        mainTl.setupWithViewPager(mainPager)
        val pageId = arguments?.getInt(MAIN_PAGE_ID_KEY) ?: 0
        setPage(pageId)

        mainPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                presenter.selectPage(position)
            }
        })
    }

    override fun setPage(pageId: Int) {
        val page = when (pageId) {
            R.id.menu_item_search -> 0
            R.id.menu_item_stations -> 1
            else -> 2
        }
        mainPager.setCurrentItem(page, true)
    }

    override fun showControls(visible: Boolean) {
        val slide = createSlideTransition()
        slide.mode = if (visible) Visibility.MODE_IN else Visibility.MODE_OUT
        TransitionManager.beginDelayedTransition(view as ViewGroup, slide)
        playerControlsFr.view?.visible(visible)
    }

    private fun createSlideTransition(): Slide {
        val slide = Slide()
        slide.slideEdge = Gravity.BOTTOM
        slide.duration = 300
        slide.addTarget(R.id.playerControlsFr)
        slide.interpolator = FastOutSlowInInterpolator()
        return slide
    }
}
