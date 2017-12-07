package io.github.vladimirmi.radius.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

abstract class BaseFragment : MvpAppCompatFragment() {

    abstract protected val layoutRes: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View
            = inflater.inflate(layoutRes, container, false)

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            activity.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}