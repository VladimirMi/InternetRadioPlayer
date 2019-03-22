package io.github.vladimirmi.internetradioplayer.presentation.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.navigation.ScreenContext
import io.github.vladimirmi.internetradioplayer.presentation.base.BackPressListener
import kotlinx.android.synthetic.main.fragment_navigation_holder.*

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

abstract class NavigationHolderFragment : Fragment(), BackPressListener {

    abstract val rootScreenContext: ScreenContext
    private lateinit var currentScreenContext: ScreenContext

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_navigation_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navigateTo(rootScreenContext)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isAdded) childFragmentManager.fragments.forEach { it.userVisibleHint = isVisibleToUser }
    }

    override fun handleBackPressed(): Boolean {
        return childFragmentManager.fragments.any { it is BackPressListener && it.handleBackPressed() }
                || backTo(currentScreenContext.parent)
    }

    private fun setupNavigation(screenContext: ScreenContext) {
        if (screenContext.parent != null) {
            parentBt.setOnClickListener { backTo(screenContext.parent) }
            parentBt.text = screenContext.parent.title
        }
        screenTitleTv.text = screenContext.title
        navigationContainer.visible(screenContext.parent != null, gone = false)

        val inflater = LayoutInflater.from(requireContext())
        childrenContainer.removeAllViews()
        screenContext.children.forEachIndexed { index, childContext ->
            val childView = childContext.createSmallView(inflater, childrenContainer)
            childView.setOnClickListener { navigateTo(childContext) }
            childrenContainer.addView(childView)
        }
        currentScreenContext = screenContext
    }

    private fun navigateTo(screenContext: ScreenContext) {
        setupNavigation(screenContext)
        val fragment = screenContext.createFragment()
        fragment.userVisibleHint = userVisibleHint
        childFragmentManager.beginTransaction()
                .replace(R.id.dataContainer, fragment)
                .addToBackStack(null)
                .commit()
    }

    private fun backTo(screenContext: ScreenContext?): Boolean {
        if (screenContext == null) return false
        setupNavigation(screenContext)
        childFragmentManager.popBackStack()
        return true
    }
}