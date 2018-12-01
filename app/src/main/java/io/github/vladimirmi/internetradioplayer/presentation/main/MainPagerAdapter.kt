package io.github.vladimirmi.internetradioplayer.presentation.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.FavoriteListFragment
import io.github.vladimirmi.internetradioplayer.presentation.player.PlayerFragment
import io.github.vladimirmi.internetradioplayer.presentation.search.SearchFragment

/**
 * Created by Vladimir Mikhalev 11.11.2018.
 */

const val PAGE_SEARCH = 0
const val PAGE_FAVORITES = 1
const val PAGE_PLAYER = 2

class MainPagerAdapter(context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val tabTitles = context.resources.getStringArray(R.array.uber_stations_tabs)

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SearchFragment()
            1 -> FavoriteListFragment()
            2 -> PlayerFragment()
            else -> throw IllegalStateException("Can't find fragment for the position $position")
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }
}
