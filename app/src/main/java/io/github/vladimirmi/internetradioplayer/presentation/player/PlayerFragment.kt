package io.github.vladimirmi.internetradioplayer.presentation.player

import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.*
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.view_station_info.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class PlayerFragment : BaseFragment<PlayerPresenter, PlayerView>(), PlayerView {

    override val layout = R.layout.fragment_player

    private var editTextBg: Int = 0

    override fun providePresenter(): PlayerPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(PlayerPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        titleEt.setEditable(false)
        setupGroupSpinner()
        editTitleBt.setOnClickListener { changeTitleEditable() }
        // save default edit text background
        val typedValue = TypedValue()
        activity?.theme?.resolveAttribute(android.R.attr.editTextBackground, typedValue, true)
        editTextBg = typedValue.resourceId

        // set appropriate action on the multiline text
        titleEt.imeOptions = EditorInfo.IME_ACTION_NEXT
        titleEt.setRawInputType(InputType.TYPE_CLASS_TEXT)

        metadataTv.isSelected = true
        playPauseBt.setOnClickListener { presenter.playPause() }
        playPauseBt.setManualMode(true)
        previousBt.setOnClickListener { presenter.skipToPrevious() }
        nextBt.setOnClickListener { presenter.skipToNext() }
//        bufferingPb.indeterminateDrawable.mutate().setTintExt(context!!.color(R.color.pause_button))
    }

    //region =============== PlayerView ==============


    override fun setStation(station: Station) {
        titleEt.setText(station.name)
        genreTv.setTextOrHide(station.genre)
        specsTv.setTextOrHide(station.specs)
    }

    private fun setupGroupSpinner() {
        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.addAll("New folder...", "Other")
        groupSpinner.adapter = adapter
        groupSpinner.setSelection(1)
    }

    override fun openLinkDialog(url: String) {
        LinkDialog.newInstance(url).show(childFragmentManager, "link_dialog")
    }

    override fun openAddShortcutDialog() {
        AddShortcutDialog().show(childFragmentManager, "add_shortcut_dialog")
    }

    override fun showStopped() {
        playPauseBt.isPlaying = false
//        bufferingPb.visible(false)
    }

    override fun showPlaying() {
        playPauseBt.isPlaying = true
//        bufferingPb.visible(false)
    }

    override fun showBuffering() {
        playPauseBt.isPlaying = true
//        bufferingPb.visible(true)
    }

    override fun showNext() {
        nextBt.bounceXAnimation(200f).start()
    }

    override fun showPrevious() {
        previousBt.bounceXAnimation(-200f).start()
    }

    override fun setMetadata(metadata: String) {
        metadataTv.text = metadata
    }

    //endregion

    private fun changeTitleEditable() {
        val enabled = titleEt.isClickable
        titleEt.setEditable(!enabled)
        editTitleIv.setImageResource(if (enabled) R.drawable.ic_edit else R.drawable.ic_submit)
        if (enabled) {
            context!!.inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
        } else {
            titleEt.setSelection(titleEt.length())
            titleEt.requestFocus()
            context!!.inputMethodManager.showSoftInput(titleEt, InputMethodManager.SHOW_IMPLICIT)
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

    private fun TextView.setTextOrHide(text: String?) {
        if (text == null || text.isBlank()) {
            visible(false)
        } else {
            visible(true)
            this.text = text
        }
    }

    private fun EditText.setEditable(enable: Boolean) {
        isClickable = enable
        isFocusable = enable
        isFocusableInTouchMode = enable
        isCursorVisible = enable
        setBackgroundResource(if (enable) editTextBg else 0)
    }
}
