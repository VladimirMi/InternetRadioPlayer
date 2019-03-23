package io.github.vladimirmi.internetradioplayer.presentation.data

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Slide
import androidx.transition.TransitionManager
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

    private fun setupNavigation(screenContext: ScreenContext, forward: Boolean) {
        setupParent(screenContext, forward)
        setupChildren(screenContext, forward)
        currentScreenContext = screenContext
    }

    private fun setupParent(screenContext: ScreenContext, forward: Boolean) {
        if (screenContext.parent != null) {
            parentBt.setOnClickListener { backTo(screenContext.parent) }
            animateParent(forward) {
                parentTitleTv.text = screenContext.parent.title
                screenTitleTv.text = screenContext.title
            }
        }
        navigationContainer.visible(screenContext.parent != null, gone = false)
    }

    private fun setupChildren(screenContext: ScreenContext, forward: Boolean) {
        val inflater = LayoutInflater.from(requireContext())
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }
        screenContext.children.forEach { child ->
            val view = child.createSmallView(inflater, childrenContainer)
            view.setOnClickListener { navigateTo(child) }
            container.addView(view)
        }
        animateChildren(container, forward)
        childrenContainer.removeAllViews()
        childrenContainer.addView(container)
    }

    private fun navigateTo(screenContext: ScreenContext) {
        setupNavigation(screenContext, forward = true)
        val fragment = screenContext.createFragment()
        fragment.userVisibleHint = userVisibleHint
        childFragmentManager.beginTransaction()
                .replace(R.id.dataContainer, fragment)
                .addToBackStack(null)
                .commit()
    }

    private fun backTo(screenContext: ScreenContext?): Boolean {
        if (screenContext == null) return false
        setupNavigation(screenContext, forward = false)
        childFragmentManager.popBackStack()
        return true
    }

    private fun animateChildren(target: View, forward: Boolean) {
        val slideIn = Slide()
                .apply { slideEdge = if (forward) Gravity.END else Gravity.START }
                .setInterpolator(FastOutSlowInInterpolator())
        slideIn.addTarget(target)

        TransitionManager.beginDelayedTransition(childrenContainer, slideIn)
    }

    private fun animateParent(forward: Boolean, onEnd: () -> Unit) {
        if (forward) {
            val startX = screenTitleTv.x
            val endX = parentTitleTv.x
            parentTitleTv.visible(false)
            screenTitleTv.animate()
                    .setInterpolator(FastOutSlowInInterpolator())
                    .x(endX)
                    .withEndAction {
                        screenTitleTv.x = startX
                        parentTitleTv.visible(true)
                        onEnd()
                    }
        } else {
            val startX = parentTitleTv.x
            val endX = (requireView().width - parentTitleTv.width).toFloat()
            screenTitleTv.visible(false)
            parentTitleTv.animate()
                    .setInterpolator(FastOutSlowInInterpolator())
                    .x(endX)
                    .withEndAction {
                        parentTitleTv.x = startX
                        screenTitleTv.visible(true)
                        onEnd()
                    }
        }
    }
}