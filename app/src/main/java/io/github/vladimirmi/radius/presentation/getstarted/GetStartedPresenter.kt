package io.github.vladimirmi.radius.presentation.getstarted

import android.net.Uri
import io.github.vladimirmi.radius.presentation.root.RootPresenter
import io.github.vladimirmi.radius.ui.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */


class GetStartedPresenter
@Inject constructor(private val rootPresenter: RootPresenter)
    : BasePresenter<GetStartedView>() {

    override fun attachView(view: GetStartedView?) {
        super.attachView(view)
        rootPresenter.viewState.showControls(false)
    }
}