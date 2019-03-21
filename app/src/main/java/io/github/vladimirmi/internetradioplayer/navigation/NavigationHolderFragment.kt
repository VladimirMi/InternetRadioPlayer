package io.github.vladimirmi.internetradioplayer.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.presentation.base.BackPressListener

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

abstract class NavigationHolderFragment : Fragment(), NavigationView {

    abstract val rootScreen: NavigationScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigateTo(rootScreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_navigation_holder, container, false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isAdded) childFragmentManager.fragments.forEach { it.userVisibleHint = isVisibleToUser }
    }

    override fun navigateTo(screen: NavigationScreen) {
        val fragment = screen.createFragment()
        fragment.userVisibleHint = userVisibleHint
        childFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun back() {
        childFragmentManager.popBackStack()
    }

    override fun handleBackPressed(): Boolean {
        return childFragmentManager.fragments.any { it is BackPressListener && it.handleBackPressed() }
    }
}