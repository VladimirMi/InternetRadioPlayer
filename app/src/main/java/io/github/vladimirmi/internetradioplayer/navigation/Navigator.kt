package io.github.vladimirmi.internetradioplayer.navigation

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.getstarted.GetStartedFragment
import io.github.vladimirmi.internetradioplayer.presentation.iconpicker.IconPickerFragment
import io.github.vladimirmi.internetradioplayer.presentation.root.RootActivity
import io.github.vladimirmi.internetradioplayer.presentation.station.StationFragment
import io.github.vladimirmi.internetradioplayer.presentation.stationlist.StationListFragment
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.*

/**
 * Created by Vladimir Mikhalev 03.12.2017.
 */

class Navigator(activity: RootActivity, containerId: Int)
    : SupportAppNavigator(activity, containerId) {

    private var currentKey = currentKeyFromBackStack(activity)

    private fun currentKeyFromBackStack(activity: RootActivity): String {
        return with(activity.supportFragmentManager) {
            if (backStackEntryCount > 0) {
                getBackStackEntryAt(backStackEntryCount - 1).name
            } else ""
        }
    }

    init {
        activity.supportFragmentManager.addOnBackStackChangedListener {
            currentKey = currentKeyFromBackStack(activity)
        }
    }

    override fun createActivityIntent(context: Context, screenKey: String, data: Any?) = null


    override fun createFragment(screenKey: String, data: Any?): Fragment? {
        if (currentKey == screenKey) return null
        val screen = screenKey.substringBefore(Router.DELIMITER)
        return when (screen) {
            Router.GET_STARTED_SCREEN -> GetStartedFragment()
            Router.STATIONS_LIST_SCREEN -> StationListFragment()
            Router.STATION_SCREEN -> StationFragment()
            Router.ICON_PICKER_SCREEN -> IconPickerFragment()
            Router.SETTINGS_SCREEN -> IconPickerFragment()
            else -> null
        }
    }

    override fun applyCommand(command: Command?) {
        if ((command is NextStation || command is PreviousStation)
                && !currentKey.contains(Router.STATION_SCREEN)) {
            return
        }
        super.applyCommand(command)
    }

    override fun setupFragmentTransactionAnimation(
            command: Command?,
            currentFragment: Fragment?,
            nextFragment: Fragment?,
            fragmentTransaction: FragmentTransaction?) {
        when (command) {
        // order matters because Next and Previous is subclasses of Forward
            is NextStation -> forwardTransition(fragmentTransaction)
            is PreviousStation -> previousTransition(fragmentTransaction)
            is ForwardReplace -> forwardReplaceTransition(fragmentTransaction)
            is Forward -> forwardTransition(fragmentTransaction)
            is Back, is BackTo -> backTransition(fragmentTransaction)
            is Replace -> replaceTransition(fragmentTransaction)
        }
    }

    private fun previousTransition(fragmentTransaction: FragmentTransaction?) {
        fragmentTransaction?.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
                R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun backTransition(fragmentTransaction: FragmentTransaction?) {
        fragmentTransaction?.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
                R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun forwardTransition(fragmentTransaction: FragmentTransaction?) {
        fragmentTransaction?.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun replaceTransition(fragmentTransaction: FragmentTransaction?) {
        fragmentTransaction?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun forwardReplaceTransition(fragmentTransaction: FragmentTransaction?) {
        fragmentTransaction?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun unknownScreen(command: Command?) {
        //do nothing
    }
}
