package io.github.vladimirmi.radius.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

abstract class BaseFragment : MvpAppCompatFragment() {

    abstract protected val layoutRes: Int

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View
            = inflater.inflate(layoutRes, container, false)
}