package io.github.vladimirmi.internetradioplayer.presentation.player

import android.os.Handler
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
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.*
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.favoritelist.NewGroupDialog
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.view_station_info.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class PlayerFragment : BaseFragment<PlayerPresenter, PlayerView>(), PlayerView, NewGroupDialog.Callback {

    override val layout = R.layout.fragment_player

    private var editTextBg: Int = 0
    private lateinit var adapter: ArrayAdapter<String>
    private var blockSpinnerSelectionListener = false

    override fun providePresenter(): PlayerPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(PlayerPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        setupTitle()
        setupGroupSpinner()

        favoriteBt.setOnClickListener { presenter.changeFavorite() }

        metadataTv.isSelected = true
        playPauseBt.setOnClickListener { presenter.playPause() }
        playPauseBt.setManualMode(true)
        previousBt.setOnClickListener { presenter.skipToPrevious() }
        nextBt.setOnClickListener { presenter.skipToNext() }
//        bufferingPb.indeterminateDrawable.mutate().setTintExt(context!!.color(R.color.pause_button))
    }

    private fun setupTitle() {
        val typedValue = TypedValue() // save default edit text background
        activity?.theme?.resolveAttribute(android.R.attr.editTextBackground, typedValue, true)
        editTextBg = typedValue.resourceId

        titleEt.setEditable(false)
        editTitleBt.setOnClickListener { changeTitleEditable() }
        titleEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                changeTitleEditable()
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
                if (blockSpinnerSelectionListener) return
                val groupName = Group.getDbName(adapter.getItem(position)!!, context!!)
                presenter.selectGroup(position, groupName)
            }
        }
    }

    //region =============== PlayerView ==============

    override fun setStation(station: Station) {
        titleEt.setText(station.name)
        genreTv.setTextOrHide(station.genre)
        specsTv.setTextOrHide(station.specs)
    }

    override fun setFavorite(isFavorite: Boolean) {
        val tint = if (isFavorite) R.color.accentColor else R.color.primaryColor
        favoriteBt.background.setTintExt(context!!.color(tint))
    }

    override fun setGroups(list: List<String>) {
        adapter.clear()
        adapter.add("New folder...")
        adapter.addAll(list.map { Group.getViewName(it, context!!) })
    }

    override fun setGroup(position: Int) {
        blockSpinnerSelectionListener = true
        groupSpinner.setSelection(position)
        Handler().postDelayed({ blockSpinnerSelectionListener = false }, 100)
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

    override fun onNewGroupCreate(group: String) {
        presenter.createGroup(group)
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
            presenter.editStationTitle(titleEt.text.toString())
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
