package io.github.vladimirmi.radius.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatDialogFragment

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

abstract class BaseDialogFragment : MvpAppCompatDialogFragment() {

    abstract protected val layoutRes: Int

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View
            = inflater.inflate(layoutRes, container, false)
}