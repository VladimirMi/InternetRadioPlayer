package io.github.vladimirmi.radius.presentation.getstarted

import android.net.Uri
import android.os.Bundle
import android.view.View
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.presentation.root.RootActivity
import io.github.vladimirmi.radius.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_getstarted.*

/**
 * Created by Vladimir Mikhalev 22.12.2017.
 */

class GetStartedFragment : BaseFragment() {

    override val layoutRes = R.layout.fragment_getstarted

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        addNewBt.setOnClickListener {
            NewStationDialogFragment().show(childFragmentManager, "new_station_dialog")
        }
    }

    fun parseUri(uri: Uri) {
        (activity as RootActivity).handleUri(uri)
    }

}