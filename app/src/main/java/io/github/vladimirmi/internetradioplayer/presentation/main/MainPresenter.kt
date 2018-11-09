package io.github.vladimirmi.internetradioplayer.presentation.main

import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

@InjectViewState
class MainPresenter
@Inject constructor(private val router: Router)
    : BasePresenter<MainView>() {

    override fun onFirstViewAttach() {
    }
}
