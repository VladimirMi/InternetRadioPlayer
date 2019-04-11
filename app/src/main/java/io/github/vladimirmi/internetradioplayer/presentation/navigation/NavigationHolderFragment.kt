package io.github.vladimirmi.internetradioplayer.presentation.navigation

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
import io.github.vladimirmi.internetradioplayer.extensions.waitForLayout
import io.github.vladimirmi.internetradioplayer.presentation.base.BackPressListener
import kotlinx.android.synthetic.main.fragment_navigation_holder.*

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

abstract class NavigationHolderFragment : Fragment(), BackPressListener {

    abstract val rootScreenContext: ScreenContext
    protected lateinit var currentScreenContext: ScreenContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentScreenContext = rootScreenContext
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_navigation_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navigateTo(currentScreenContext, animate = false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isAdded) childFragmentManager.fragments.forEach { it.userVisibleHint = isVisibleToUser }
    }

    override fun handleBackPressed(): Boolean {
        return childFragmentManager.fragments.any { it is BackPressListener && it.handleBackPressed() }
                || backTo(currentScreenContext.parent)
    }

    private fun navigateTo(screenContext: ScreenContext, animate: Boolean) {
        setupNavigation(screenContext, animate, isForward = true)
        val fragment = screenContext.createFragment() ?: return
        fragment.userVisibleHint = userVisibleHint
        childFragmentManager.beginTransaction()
                .replace(R.id.dataContainer, fragment)
                .addToBackStack(null)
                .commit()

    }

    private fun backTo(screenContext: ScreenContext?): Boolean {
        if (screenContext == null) return false
        childFragmentManager.popBackStack()
        setupNavigation(screenContext, animate = true, isForward = false)
        return true
    }

    private fun setupNavigation(screenContext: ScreenContext, animate: Boolean, isForward: Boolean) {
        setupParent(screenContext, animate, isForward)
        setupChildren(screenContext, animate, isForward)
        currentScreenContext = screenContext
    }

    private fun setupParent(screenContext: ScreenContext, animate: Boolean, isForward: Boolean) {
        parentBt.setOnClickListener { backTo(screenContext.parent) }
        parentBt.visible(screenContext.parent != null, gone = false)
        animateParent(animate, isForward) {
            parentTitleTv.visible(screenContext.parent != null, gone = false)
            parentTitleTv.text = screenContext.parent?.title
            screenTitleTv.text = screenContext.title
        }
    }

    private fun setupChildren(screenContext: ScreenContext, animate: Boolean, isForward: Boolean) {
        val inflater = LayoutInflater.from(requireContext())
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }
        screenContext.children.forEach { child ->
            val view = child.createSmallView(inflater, childrenContainer)
            view.setOnClickListener { navigateTo(child, animate = true) }
            container.addView(view)
        }
        screenContext.parent?.position = childrenScroll.scrollY
        animateChildren(container, animate, isForward)
        childrenContainer.removeAllViews()
        childrenContainer.addView(container)
        if (isForward) {
            screenContext.position = 0
        } else {
            childrenContainer.waitForLayout {
                childrenScroll.scrollTo(0, screenContext.position); true
            }
        }
    }

    private fun animateChildren(target: View, animate: Boolean, isForward: Boolean) {
        if (!animate) return
        val slideIn = Slide()
                .apply { slideEdge = if (isForward) Gravity.END else Gravity.START }
                .setInterpolator(FastOutSlowInInterpolator())
        slideIn.addTarget(target)

        TransitionManager.beginDelayedTransition(childrenContainer, slideIn)
    }

    private fun animateParent(animate: Boolean, isForward: Boolean, onEnd: () -> Unit) {
        if (!animate) {
            onEnd(); return
        }
        if (isForward) {
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