package io.github.vladimirmi.internetradioplayer.navigation

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.iconpicker.IconPickerFragment
import io.github.vladimirmi.internetradioplayer.presentation.main.MainFragment
import io.github.vladimirmi.internetradioplayer.presentation.root.RootActivity
import io.github.vladimirmi.internetradioplayer.presentation.settings.SettingsFragment
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.*

/**
 * Created by Vladimir Mikhalev 03.12.2017.
 */

class Navigator(activity: RootActivity, containerId: Int)
    : SupportAppNavigator(activity, containerId) {

    override fun createActivityIntent(context: Context, screenKey: String, data: Any?) = null

    override fun createFragment(screenKey: String, data: Any?): Fragment? {
        return when (screenKey) {
            Router.MAIN_SCREEN -> MainFragment()
            Router.ICON_PICKER_SCREEN -> IconPickerFragment()
            Router.SETTINGS_SCREEN -> SettingsFragment()
            else -> null
        }
    }

    override fun setupFragmentTransactionAnimation(
            command: Command,
            currentFragment: Fragment?,
            nextFragment: Fragment?,
            fragmentTransaction: FragmentTransaction) {
        when (command) {
            is Forward -> forwardTransition(fragmentTransaction)
            is Back, is BackTo -> backTransition(fragmentTransaction)
            is Replace -> replaceTransition(fragmentTransaction)
        }
    }

    private fun backTransition(fragmentTransaction: FragmentTransaction) {
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
                R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun forwardTransition(fragmentTransaction: androidx.fragment.app.FragmentTransaction?) {
        fragmentTransaction?.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun replaceTransition(fragmentTransaction: androidx.fragment.app.FragmentTransaction?) {
        fragmentTransaction?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
