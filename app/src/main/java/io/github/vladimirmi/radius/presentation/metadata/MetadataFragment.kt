package io.github.vladimirmi.radius.presentation.metadata

import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.remove
import io.github.vladimirmi.radius.extensions.show
import io.github.vladimirmi.radius.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_metadata.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 08.12.2017.
 */

class MetadataFragment : BaseFragment(), MetadataView {
    override val layoutRes = R.layout.fragment_metadata

    @InjectPresenter
    lateinit var presenter: MetadataPresenter

    @ProvidePresenter
    fun providePresenter(): MetadataPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(MetadataPresenter::class.java).also {
            Toothpick.closeScope(this)
        }
    }

    //region =============== MetadataView ==============

    override fun setInfo(string: String) {
        view?.show()
        metadataTv.isSelected = true
        metadataTv.text = string
    }

    override fun hide() {
        view?.remove()
    }

    //endregion
}