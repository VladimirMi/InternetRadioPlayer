package io.github.vladimirmi.radius.presentation.root

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.Screens
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.mediaList.MediaListFragment
import io.github.vladimirmi.radius.presentation.station.StationFragment
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.BackTo
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward

/**
 * Created by Vladimir Mikhalev 03.12.2017.
 */

class Navigator(private val activity: FragmentActivity, containerId: Int)
    : SupportAppNavigator(activity, containerId) {

    private var currentKey: String? = null

    override fun createActivityIntent(screenKey: String, data: Any?) = null

    override fun createFragment(screenKey: String, data: Any?): Fragment? {
        if (currentKey == screenKey) return null
        return when (screenKey) {
            Screens.MEDIA_LIST_SCREEN -> MediaListFragment()
            Screens.STATION_SCREEN -> StationFragment.newInstance(data as Station)
            else -> null
        }
    }

    override fun applyCommand(command: Command?) {
        currentKey = with(activity.supportFragmentManager) {
            if (backStackEntryCount > 0) {
                getBackStackEntryAt(backStackEntryCount - 1)?.name
            } else null
        }
        super.applyCommand(command)
    }

    override fun setupFragmentTransactionAnimation(command: Command?, currentFragment: Fragment?, nextFragment: Fragment?, fragmentTransaction: FragmentTransaction?) {
        when (command) {
            is Forward -> {
                fragmentTransaction?.setCustomAnimations(R.anim.slide_in_right,
                        R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
            }
            is Back, is BackTo -> {
                fragmentTransaction?.setCustomAnimations(R.anim.slide_in_left,
                        R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    override fun unknownScreen(command: Command?) {
        //do nothing
    }
}