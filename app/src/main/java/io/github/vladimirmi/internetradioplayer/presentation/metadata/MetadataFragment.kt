package io.github.vladimirmi.internetradioplayer.presentation.metadata

import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.ui.base.BaseFragment
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

    override fun setMetadata(string: String) {
        metadataTv.text = string
    }

    override fun setMetadata(resId: Int) {
        metadataTv.text = context.getString(resId)
    }

    override fun hide() {
        view?.visible(false)
    }

    override fun show() {
        view?.visible(true)
        metadataTv.isSelected = true
    }

    //endregion
}
