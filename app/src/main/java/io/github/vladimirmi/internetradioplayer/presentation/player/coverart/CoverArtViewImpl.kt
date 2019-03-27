package io.github.vladimirmi.internetradioplayer.presentation.player.coverart

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.dp
import io.github.vladimirmi.internetradioplayer.extensions.themeAttrData
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseCustomView
import kotlinx.android.synthetic.main.view_cover_art.view.*
import mkaflowski.mediastylepalette.MediaNotificationProcessor
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
                .asBitmap()
                .load(uri)
                .transition(BitmapTransitionOptions.withCrossFade())
                .addListener(requestListener)
                .error(R.drawable.ic_station_3)
                .placeholder(getProgressDrawable())
                .into(coverArtIv)
    }

    private val requestListener = object : RequestListener<Bitmap> {
        override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
            return false
        }

        override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
            generatePalette(resource)
            return false
        }
    }

    private fun generatePalette(bitmap: Bitmap) {
        val processor = MediaNotificationProcessor(context)
        processor.getPaletteAsync({
            coverArtContainer.setBackgroundColor(it.backgroundColor)
        }, bitmap)
    }

    private fun getProgressDrawable(): CircularProgressDrawable {
        val progressDrawable = CircularProgressDrawable(context)
        progressDrawable.strokeWidth = (6 * context.dp).toFloat()
        progressDrawable.centerRadius = (20 * context.dp).toFloat()
        progressDrawable.setColorSchemeColors(context.themeAttrData(R.attr.colorSecondary))
        progressDrawable.start()
        return progressDrawable
    }
}
