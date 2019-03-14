package io.github.vladimirmi.internetradioplayer.presentation.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.FavoriteListFragment
import io.github.vladimirmi.internetradioplayer.presentation.history.HistoryFragment
import io.github.vladimirmi.internetradioplayer.presentation.search.SearchFragment

/**
 * Created by Vladimir Mikhalev 11.11.2018.
 */

const val PAGE_SEARCH = 0
const val PAGE_FAVORITES = 1
const val PAGE_HISTORY = 2

class MainPagerAdapter(context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val tabTitles = context.resources.getStringArray(R.array.main_tabs)

    override fun getItem(position: Int): Fragment {
        return when (position) {
            PAGE_SEARCH -> SearchFragment()
            PAGE_FAVORITES -> FavoriteListFragment()
            else -> HistoryFragment()
        }
    }

    override fun getCount(): Int {
        return tabTitles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }
}
