package io.github.vladimirmi.internetradioplayer.presentation.player.coverart

import android.content.Context
import android.util.AttributeSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseCustomView
import kotlinx.android.synthetic.main.view_cover_art.view.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 28.03.2019.
 */

class CoverArtViewImpl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseCustomView<CoverArtPresenter, CoverArtView>(context, attrs, defStyleAttr), CoverArtView {

    override val layout = R.layout.view_cover_art

    override fun providePresenter(): CoverArtPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(CoverArtPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView() {
    }

    override fun setCoverArt(uri: String) {
        Glide.with(this)
                .load(uri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(coverArtIv)
    }
}