package io.github.vladimirmi.internetradioplayer.presentation.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.station.StationFragment
import io.github.vladimirmi.internetradioplayer.presentation.stationlist.StationListFragment

/**
 * Created by Vladimir Mikhalev 11.11.2018.
 */

class MainPagerAdapter(context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val tabTitles = context.resources.getStringArray(R.array.uber_stations_tabs)

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> StationListFragment()
            1 -> StationListFragment()
            2 -> StationFragment()
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
