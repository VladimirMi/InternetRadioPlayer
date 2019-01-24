package io.github.vladimirmi.internetradioplayer.presentation.player

import android.os.Handler
import android.support.v4.media.MediaMetadataCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.service.EMPTY_METADATA
import io.github.vladimirmi.internetradioplayer.data.service.artist
import io.github.vladimirmi.internetradioplayer.data.service.isEmpty
import io.github.vladimirmi.internetradioplayer.data.service.title
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.*
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.NewGroupDialog
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.view_controls.*
import kotlinx.android.synthetic.main.view_station_info.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class PlayerFragment : BaseFragment<PlayerPresenter, PlayerView>(), PlayerView, NewGroupDialog.Callback {

    override val layout = R.layout.fragment_player

    private var editTextBg: Int = 0
    private lateinit var adapter: ArrayAdapter<String>
    private var blockSpinnerSelection = false

    override fun providePresenter(): PlayerPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(PlayerPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        setupTitle()
        setupGroupSpinner()

        favoriteBt.setOnClickListener { presenter.switchFavorite() }
        addShortcutBt.setOnClickListener { openAddShortcutDialog() }

        metaTitleTv.isSelected = true
        metaSubtitleTv.isSelected = true
        playPauseBt.setOnClickListener { presenter.playPause() }
        playPauseBt.setManualMode(true)
        previousBt.setOnClickListener { presenter.skipToPrevious() }
        nextBt.setOnClickListener { presenter.skipToNext() }
        stopBt.setOnClickListener { presenter.stop() }
        equalizerBt.setOnClickListener { presenter.openEqualizer() }
    }

    private fun setupTitle() {
        val typedValue = TypedValue() // save default edit text background
        activity?.theme?.resolveAttribute(android.R.attr.editTextBackground, typedValue, true)
        editTextBg = typedValue.resourceId

        titleEt.setEditable(false)
        editTitleBt.setOnClickListener { presenter.editStationTitle(titleEt.text.toString()) }
        titleEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                presenter.editStationTitle(titleEt.text.toString())
                true
            } else false
        }
    }

    private fun setupGroupSpinner() {
        adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        groupSpinner.adapter = adapter
        groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (blockSpinnerSelection) return
                val groupName = Group.getDbName(adapter.getItem(position)!!, context!!)
                presenter.selectGroup(position, groupName)
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (view != null && !isVisibleToUser && titleEt.isClickable) {
            requireContext().inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }

    override fun handleBackPressed(): Boolean {
        if (titleEt.isClickable) {
            switchTitleEditable()
            return true
        }
        return super.handleBackPressed()
    }

    //region =============== PlayerView ==============

    override fun setStation(station: Station) {
        titleEt.setText(station.name)
        specsTv.setTextOrHide(station.specs)
    }

    override fun setFavorite(isFavorite: Boolean) {
        val tint = if (isFavorite) R.color.orange_500 else R.color.primary_light
        favoriteBt.background.setTintExt(context!!.color(tint))
        groupSpinnerWrapper.visible(isFavorite)
        editTitleBt.visible(isFavorite)
    }

    override fun setGroups(list: List<String>) {
        adapter.clear()
        //todo to strings
        adapter.add("New folder...")
        adapter.addAll(list.map { Group.getViewName(it, context!!) })
    }

    override fun setGroup(position: Int) {
        //todo try refactor
        blockSpinnerSelection = true
        groupSpinner.setSelection(position)
        Handler().postDelayed({ blockSpinnerSelection = false }, 100)
    }

    override fun openLinkDialog(url: String) {
        LinkDialog.newInstance(url).show(childFragmentManager, "link_dialog")
    }

    override fun openAddShortcutDialog() {
        AddShortcutDialog().show(childFragmentManager, "add_shortcut_dialog")
    }

    override fun openNewGroupDialog() {
        NewGroupDialog().show(childFragmentManager, "new_group_dialog")
    }

    override fun onGroupCreate(group: String) {
        presenter.createGroup(group)
    }

    override fun onCancelGroupCreate() {
        presenter.setupGroups()
    }

    override fun showStopped() {
        playPauseBt.setPlaying(false, userVisibleHint)
        bufferingPb.visible(false)
    }

    override fun showPlaying() {
        playPauseBt.setPlaying(true, userVisibleHint)
        bufferingPb.visible(false)
    }

    override fun showBuffering() {
        playPauseBt.setPlaying(true, userVisibleHint)
        bufferingPb.visible(true)
        setMetadata(EMPTY_METADATA)
    }

    override fun showNext() {
        nextBt.bounceXAnimation(200f).start()
    }

    override fun showPrevious() {
        previousBt.bounceXAnimation(-200f).start()
    }

    override fun setMetadata(metadata: MediaMetadataCompat) {
        if (metadataCv == null) return
        val visible = !metadata.isEmpty()
        val scale = if (visible) 1f else 0f

        metadataCv.animate()
                .scaleX(scale).scaleY(scale)
                .setDuration(300)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        if (visible) metadataCv.visible(true)
        else Handler().postDelayed({ metadataCv?.visible(false) }, 300)
        with(metadata) {
            metaTitleTv.setTextOrHide(artist)
            metaSubtitleTv.setTextOrHide(title)
        }
    }


    override fun showPlaceholder(show: Boolean) {
        infoCv.visible(!show)
        controlsView.visible(!show)
        placeholderView.visible(show)
    }

    override fun switchTitleEditable() {
        val enabled = titleEt.isClickable
        titleEt.setEditable(!enabled)
        editTitleBt.setImageResource(if (enabled) R.drawable.ic_edit else R.drawable.ic_submit)
        if (enabled) {
            requireContext().inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
        } else {
            titleEt.setSelection(titleEt.length())
            titleEt.requestFocus()
            requireContext().inputMethodManager.showSoftInput(titleEt, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    //endregion

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
