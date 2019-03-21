package io.github.vladimirmi.internetradioplayer.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.setTextOrHide
import kotlinx.android.synthetic.main.fragment_navigation.*
import kotlinx.android.synthetic.main.fragment_navigation.view.*

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

class NavigationFragment : Fragment(), NavigationView {

    companion object {
        private const val EXTRA_NAV_SCREEN = "EXTRA_NAV_SCREEN"

        fun newInstance(screen: NavigationScreen): NavigationFragment {
            val args = Bundle().apply { putString(NavigationFragment.EXTRA_NAV_SCREEN, screen.title) }
            return NavigationFragment().apply { arguments = args }
        }
    }

    private lateinit var navigationScreen: NavigationScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val key = arguments?.getString(EXTRA_NAV_SCREEN) ?: return
        navigationScreen = NavigationTree.findScreen(key)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_navigation, container, false)
        val childrenContainer = view.childrenContainer
        for (child in navigationScreen.children) {
            val childView = child.createSmallView(inflater, childrenContainer)
            childView.setOnClickListener { navigateTo(child) }
            childrenContainer.addView(childView)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        parentBt.setTextOrHide(navigationScreen.parent?.title)
        parentBt.setOnClickListener { back() }
    }

    override fun navigateTo(screen: NavigationScreen) {
        (parentFragment as? NavigationView)?.navigateTo(screen)
    }

    override fun back() {
        (parentFragment as? NavigationView)?.back()
    }

    override fun handleBackPressed(): Boolean {
        return if (navigationScreen.parent != null) {
            back()
            true
        } else false
    }
}
