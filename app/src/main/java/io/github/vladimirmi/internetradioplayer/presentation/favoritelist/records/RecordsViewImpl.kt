package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.records

import android.content.Context
import android.util.AttributeSet
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFrameView
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

class RecordsViewImpl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseFrameView<RecordsPresenter, RecordsView>(context, attrs, defStyleAttr), RecordsView {

    override fun providePresenter(): RecordsPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(RecordsPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView() {

    }
}