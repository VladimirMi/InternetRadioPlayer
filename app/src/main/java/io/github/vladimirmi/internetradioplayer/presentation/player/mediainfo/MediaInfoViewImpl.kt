package io.github.vladimirmi.internetradioplayer.presentation.player.mediainfo

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.setTextOrHide
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseCustomView
import kotlinx.android.synthetic.main.view_media_info.view.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 27.03.2019.
 */

class MediaInfoViewImpl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseCustomView<MediaInfoPresenter, MediaInfoView>(context, attrs, defStyleAttr), MediaInfoView {


    override val layout = R.layout.view_media_info

    override fun providePresenter(): MediaInfoPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(MediaInfoPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView() {
        recordBt.setOnClickListener { presenter.startStopRecording() }
        addShortcutBt.setOnClickListener { openAddShortcutDialog() }
        equalizerBt.setOnClickListener { presenter.openEqualizer() }
    }

    override fun setMedia(media: Media) {
        descTv.setTextOrHide(media.description)
        groupTv.setTextOrHide(Group.getViewName(media.group, context))
        genreTv.setTextOrHide(media.genre)
        specsTv.setTextOrHide(media.specs)
        langTv.setTextOrHide(media.language)
        locationTv.setTextOrHide(media.location)
        websiteTv.setTextOrHide(media.url)

        recordBt.visible(media is Station)
        addShortcutBt.visible(media is Station)
        equalizerBt.visible(media is Station)
    }

    override fun setRecording(isRecording: Boolean) {
        val tint = context.color(if (isRecording) R.color.secondary else R.color.primary_variant)
        recordBt.setColorFilter(tint)
    }

    private fun openAddShortcutDialog() {
        fragmentManager {
            AddShortcutDialog().show(it, "add_shortcut_dialog")
        }
    }

    private fun openLinkDialog(url: String) {
        fragmentManager {
            LinkDialog.newInstance(url).show(it, "link_dialog")
        }
    }

    private fun TextView.linkStyle(enable: Boolean) {
        val string = text.toString()
        val color = ContextCompat.getColor(context, R.color.blue_500)
        text = if (enable) {
            val spannable = SpannableString(string)
            spannable.setSpan(URLSpan(string), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(ForegroundColorSpan(color), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable
        } else {
            string
        }
    }

    private fun fragmentManager(block: (FragmentManager) -> Unit) {
        (context as? FragmentActivity)?.let { block(it.supportFragmentManager) }
    }
}