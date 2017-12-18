package io.github.vladimirmi.radius.navigation

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.presentation.iconpicker.IconPickerFragment
import io.github.vladimirmi.radius.presentation.mediaList.MediaListFragment
import io.github.vladimirmi.radius.presentation.root.RootActivity
import io.github.vladimirmi.radius.presentation.station.StationFragment
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.BackTo
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward

/**
 * Created by Vladimir Mikhalev 03.12.2017.
 */

class Navigator(activity: RootActivity, containerId: Int)
    : SupportAppNavigator(activity, containerId) {

    private var currentKey = ""

    init {
        with(activity.supportFragmentManager) {
            addOnBackStackChangedListener {
                currentKey = if (backStackEntryCount > 0) {
                    getBackStackEntryAt(backStackEntryCount - 1).name
                } else {
                    Router.MEDIA_LIST_SCREEN
                }
            }
        }
    }

    override fun createActivityIntent(screenKey: String, data: Any?) = null

    override fun createFragment(screenKey: String, data: Any?): Fragment? {
        if (currentKey == screenKey) return null
        val id = screenKey.substringAfter(Router.DELIMITER)
        val screen = screenKey.substringBefore(Router.DELIMITER)
        return when (screen) {
            Router.MEDIA_LIST_SCREEN -> MediaListFragment()
        //todo remove new instance
            Router.STATION_SCREEN -> StationFragment.newInstance(id)
            Router.ICON_PICKER_SCREEN -> IconPickerFragment()
            else -> null
        }
    }

    override fun applyCommand(command: Command?) {
        if ((command is Next || command is Previous) && !currentKey.contains(Router.STATION_SCREEN)) {
            return
        }
        super.applyCommand(command)
    }

    override fun setupFragmentTransactionAnimation(command: Command?, currentFragment: Fragment?, nextFragment: Fragment?, fragmentTransaction: FragmentTransaction?) {
        when (command) {
        // order matters because Next and Previous is subclasses of Forward
            is Next -> forwardTransition(fragmentTransaction)
            is Previous -> previousTransition(fragmentTransaction)
            is Forward -> forwardTransition(fragmentTransaction)
            is Back, is BackTo -> backTransition(fragmentTransaction)
        }
    }

    private fun previousTransition(fragmentTransaction: FragmentTransaction?) {
        fragmentTransaction?.setCustomAnimations(R.anim.slide_in_left,
                R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun backTransition(fragmentTransaction: FragmentTransaction?) {
        fragmentTransaction?.setCustomAnimations(R.anim.slide_in_left,
                R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun forwardTransition(fragmentTransaction: FragmentTransaction?) {
        fragmentTransaction?.setCustomAnimations(R.anim.slide_in_right,
                R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun unknownScreen(command: Command?) {
        //do nothing
    }

    override fun exit() {
        super.exit()
        System.exit(0)
    }
}