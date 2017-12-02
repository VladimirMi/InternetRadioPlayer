package io.github.vladimirmi.radius.presentation.station

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.remove
import io.github.vladimirmi.radius.extensions.show
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.root.RootActivity
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.ui.TagView
import io.github.vladimirmi.radius.ui.base.BaseFragment
import io.github.vladimirmi.radius.ui.base.SimpleDialog
import kotlinx.android.synthetic.main.fragment_station.*
import kotlinx.android.synthetic.main.part_station_info.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class StationFragment : BaseFragment(), StationView {
    override val layoutRes = R.layout.fragment_station

    companion object {
        fun newInstance(station: Station): StationFragment {
            return StationFragment().apply {
                //todo "id" to constant
                arguments = Bundle().apply { putString("id", station.id) }
            }
        }
    }

    private var editTextBg: Int = 0
    private val dialogEdit: SimpleDialog by lazy {
        SimpleDialog(view as ViewGroup)
                .setMessage(getString(R.string.dialog_submit_message))
    }
    private val dialogDelete: SimpleDialog by lazy {
        SimpleDialog(view as ViewGroup)
                .setMessage(getString(R.string.dialog_remove_message))
    }

    @InjectPresenter lateinit var presenter: StationPresenter

    @ProvidePresenter
    fun providePresenter(): StationPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(StationPresenter::class.java).also {
            Toothpick.closeScope(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        presenter.id = arguments.getString("id")
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val typedValue = TypedValue()
        activity.theme.resolveAttribute(android.R.attr.editTextBackground, typedValue, true)
        editTextBg = typedValue.resourceId
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //todo move to base class and setHasOptionsMenu()
        if (item?.itemId == android.R.id.home) {
            activity.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

//region =============== StationView ==============

    override fun buildToolbar(builder: ToolbarBuilder) {
        builder.build(activity as RootActivity)
    }

    override fun setStation(station: Station) {
        title.setTextWithoutAnimation(station.title)
        group.setTextWithoutAnimation(station.group)
        uri.setTextWithoutAnimation(station.uri)
        url.setTextWithoutAnimation(station.url)
        bitrate.setTextWithoutAnimation(station.bitrate.toString())
        sample.setTextWithoutAnimation(station.sample.toString())
        station.genre.forEach { flex_box.addView(TagView(context, it, null)) }
    }

    override fun setEditable(editable: Boolean) {
        title.setEditable(editable)

        group.setEditable(editable)
        if (editable) {
            group.show()
        } else if (group.isBlank()) group.remove()

        uri.setEditable(editable)

        url.setEditable(editable)
        if (editable) {
            url.show()
        } else if (url.isBlank()) url.remove()

        bitrate.setEditable(editable)
        //todo strings from res(units)
        bitrate.cutOff(editable, "kbps")

        sample.setEditable(editable)
        sample.cutOff(editable, "Hz")
    }

    override fun openEditDialog() {
        dialogEdit.setPositiveAction { presenter.edit(constructStation()) }
                .setNegativeAction { presenter.cancelEdit() }
                .show()
    }

    override fun closeEditDialog() {
        dialogEdit.dismiss()
    }

    override fun openDeleteDialog() {
        dialogDelete.setPositiveAction { presenter.delete() }
                .setNegativeAction { presenter.cancelDelete() }
                .show()
    }

    override fun closeDeleteDialog() {
        dialogDelete.dismiss()
    }

    //endregion

    private fun constructStation(): Station {
        val genres = ArrayList<String>()
        (0 until flex_box.childCount)
                .forEach {
                    val tagView = flex_box.getChildAt(it) as TagView
                    genres.add(tagView.text.toString())
                }
        return Station(
                id = presenter.id,
                uri = uri.editText!!.text.toString(),
                title = title.editText!!.text.toString(),
                group = group.editText!!.text.toString(),
                genre = genres,
                url = url.editText!!.text.toString(),
                sample = sample.editText!!.text.toString().toInt(),
                bitrate = bitrate.editText!!.text.toString().toInt()
        )
    }

    private fun TextInputLayout.setTextWithoutAnimation(string: String) {
        isHintAnimationEnabled = false
        editText?.setText(string)
        isHintAnimationEnabled = true
    }

    private fun TextInputLayout.setEditable(enable: Boolean) {
        editText?.isFocusable = enable
        editText?.isClickable = enable
        editText?.isFocusableInTouchMode = enable
        editText?.isCursorVisible = enable

        if (enable) editText?.setBackgroundResource(editTextBg)
        else editText?.setBackgroundResource(0)
    }

    private fun TextInputLayout.cutOff(editable: Boolean, suffix: String) {
        val s = editText?.text.toString()
        val new = if (editable) {
            val value = s.substringBeforeLast(suffix)
            //todo strings to res
            if (value == "n/a") "0" else value
        } else {
            if (s == "0" || s.isBlank()) "n/a" else s + suffix
        }
        setTextWithoutAnimation(new)
    }

    private fun TextInputLayout.isBlank() = editText?.text?.isBlank() ?: true
}


