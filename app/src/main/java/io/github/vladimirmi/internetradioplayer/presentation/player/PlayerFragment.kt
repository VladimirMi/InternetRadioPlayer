package io.github.vladimirmi.internetradioplayer.presentation.player

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
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
        // save default edit text background
//        val typedValue = TypedValue()
//        activity?.theme?.resolveAttribute(android.R.attr.editTextBackground, typedValue, true)
//        editTextBg = typedValue.resourceId

        // set appropriate action on the multiline text
//        titleTil.imeOptions = EditorInfo.IME_ACTION_NEXT
//        titleTil.setRawInputType(InputType.TYPE_CLASS_TEXT)
    }


    //region =============== PlayerView ==============

    override fun setStation(station: Station) {
        titleEt.setText(station.name)
        genreTv.text = station.genre
        specsTv.text = station.specs

        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.addAll("New folder...", "Other")
        groupSpinner.adapter = adapter
        groupSpinner.setSelection(1)
    }

    override fun setEditMode(editMode: Boolean) {
//        titleTil.setEditable(editMode)
//        val groupVisible = groupEt.text.isNotBlank() || editMode
//        groupLabelTv.visible(groupVisible)
//        groupEt.visible(groupVisible)
//        groupEt.setEditable(editMode)
//
//        if (editMode) {
//            titleTil.requestFocus()
//            titleTil.setSelection(titleTil.text.length)
//        } else {
//            context!!.inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
//        }
    }

    override fun openLinkDialog(url: String) {
        LinkDialog.newInstance(url).show(childFragmentManager, "link_dialog")
    }

    override fun openAddShortcutDialog() {
        AddShortcutDialog().show(childFragmentManager, "add_shortcut_dialog")
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

    private fun openLink(it: TextView) {

    }

    private fun EditText.setEditable(enable: Boolean) {
        isFocusable = enable
        isClickable = enable
        isFocusableInTouchMode = enable
        isCursorVisible = enable

        if (enable) setBackgroundResource(editTextBg)
        else setBackgroundResource(0)
    }
}
