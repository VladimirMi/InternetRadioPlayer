package io.github.vladimirmi.radius.presentation.station

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.inputMethodManager
import io.github.vladimirmi.radius.extensions.remove
import io.github.vladimirmi.radius.extensions.show
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.source.StationIconSource
import io.github.vladimirmi.radius.presentation.root.RootActivity
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.ui.TagView
import io.github.vladimirmi.radius.ui.base.BackPressListener
import io.github.vladimirmi.radius.ui.base.BaseFragment
import io.github.vladimirmi.radius.ui.base.SimpleDialog
import kotlinx.android.synthetic.main.fragment_station.*
import kotlinx.android.synthetic.main.part_station_info.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class StationFragment : BaseFragment(), StationView, BackPressListener {
    override val layoutRes = R.layout.fragment_station

    companion object {
        fun newInstance(stationId: String): StationFragment {
            return StationFragment().apply {
                //todo "id" to constant
                arguments = Bundle().apply { putString("id", stationId) }
            }
        }
    }

    private val iconSource = Scopes.app.getInstance(StationIconSource::class.java)

    private var editTextBg: Int = 0
    private val dialogSave: SimpleDialog by lazy {
        SimpleDialog(view as ViewGroup)
                .setMessage(getString(R.string.dialog_submit_message))
    }
    private val dialogDelete: SimpleDialog by lazy {
        SimpleDialog(view as ViewGroup)
                .setMessage(getString(R.string.dialog_remove_message))
    }
    private val dialogLink: SimpleDialog by lazy {
        SimpleDialog(view as ViewGroup)
                .setMessage(getString(R.string.dialog_goto_message))
    }
    private val dialogCancelEdit: SimpleDialog by lazy {
        SimpleDialog(view as ViewGroup)
                .setMessage(getString(R.string.dialog_cancel_edit_message))
    }
    private val dialogCreate: SimpleDialog by lazy {
        SimpleDialog(view as ViewGroup)
                .setMessage(getString(R.string.dialog_create_message))
    }
    private val dialogCancelCreate: SimpleDialog by lazy {
        SimpleDialog(view as ViewGroup)
                .setMessage(getString(R.string.dialog_cancel_create_message))
    }

    @InjectPresenter
    lateinit var presenter: StationPresenter

    @ProvidePresenter
    fun providePresenter(): StationPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(StationPresenter::class.java).also {
            Toothpick.closeScope(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.id = arguments.getString("id")
    }

    override fun onStop() {
        super.onStop()
        closeDeleteDialog()
        closeSaveDialog()
        closeLinkDialog()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val typedValue = TypedValue()
        activity.theme.resolveAttribute(android.R.attr.editTextBackground, typedValue, true)
        editTextBg = typedValue.resourceId

        urlTil.editText?.setOnClickListener { presenter.openLink((it as EditText).text.toString()) }
        uriTil.editText?.setOnClickListener { presenter.openLink((it as EditText).text.toString()) }
        fab.setOnClickListener { presenter.changeMode() }
        changeIconBt.setOnClickListener { presenter.changeIcon() }
        changeIconBt.background.alpha = 128
    }

    override fun onBackPressed() = presenter.onBackPressed()

    //region =============== StationView ==============

    override fun buildToolbar(builder: ToolbarBuilder) {
        builder.build(activity as RootActivity)
    }

    override fun setStation(station: Station) {
        iconIv.setImageBitmap(iconSource.getBitmap(station.title))
        titleTil.setTextWithoutAnimation(station.title)
        folderTil.setTextWithoutAnimation(station.group)
        uriTil.setTextWithoutAnimation(station.uri)
        urlTil.setTextWithoutAnimation(station.url)
        bitrateTil.setTextWithoutAnimation(station.bitrate.toString())
        sampleTil.setTextWithoutAnimation(station.sample.toString())
        station.genre.forEach { genresFl.addView(TagView(context, it, null)) }
    }

    override fun setEditMode(editMode: Boolean) {
        titleTil.setEditable(editMode)
        folderTil.setEditable(editMode)
        uriTil.setEditable(editMode)
        urlTil.setEditable(editMode)
        bitrateTil.setEditable(editMode)
        bitrateTil.cutOff(editMode, getString(R.string.unit_bitrate))
        sampleTil.setEditable(editMode)
        sampleTil.cutOff(editMode, getString(R.string.unit_sample_rate))
        uriTil.linkStyle(!editMode)
        urlTil.linkStyle(!editMode)

        if (editMode) {
            changeIconBt.show()
            fab.setImageResource(R.drawable.ic_submit)
            folderTil.show()
            urlTil.show()
        } else {
            changeIconBt.remove()
            fab.setImageResource(R.drawable.ic_edit)
            if (folderTil.isBlank()) folderTil.remove()
            if (urlTil.isBlank()) urlTil.remove()
            view?.clearFocus()
            context.inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }

    override fun openSaveDialog() {
        val station = constructStation()
        if (presenter.isChanged(station)) {
            dialogSave.setPositiveAction { presenter.edit(station) }
                    .setNegativeAction { presenter.edit(null) }
                    .show()
        } else {
            presenter.viewMode()
        }
    }

    override fun closeSaveDialog() {
        dialogSave.dismiss()

    }

    override fun openDeleteDialog() {
        dialogDelete.setPositiveAction { presenter.delete(true) }
                .setNegativeAction { presenter.delete(false) }
                .show()
    }

    override fun closeDeleteDialog() {
        dialogDelete.dismiss()
    }

    override fun openLinkDialog(url: String) {
        dialogLink.setPositiveAction { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                .setNegativeAction { presenter.cancelLink() }
                .show()
    }

    override fun closeLinkDialog() {
        dialogLink.dismiss()
    }

    override fun openCancelEditDialog() {
        dialogCancelEdit.setPositiveAction { presenter.cancelEdit(true) }
                .setNegativeAction { presenter.cancelEdit(false) }
                .show()
    }

    override fun closeCancelEditDialog() {
        dialogCancelEdit.dismiss()
    }

    override fun openCreateDialog() {
        dialogCreate.setPositiveAction { presenter.create(constructStation()) }
                .setNegativeAction { presenter.create(null) }
                .show()
    }

    override fun closeCreateDialog() {
        dialogCreate.dismiss()
    }

    override fun openCancelCreateDialog() {
        dialogCancelCreate.setPositiveAction { presenter.cancelCreate(true) }
                .setNegativeAction { presenter.cancelCreate(false) }
                .show()
    }

    override fun closeCancelCreateDialog() {
        dialogCancelCreate.dismiss()
    }

    override fun showToast(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    //endregion

    private fun constructStation(): Station {
        val genres = ArrayList<String>()
        (0 until genresFl.childCount)
                .forEach {
                    val tagView = genresFl.getChildAt(it) as TagView
                    genres.add(tagView.text.toString())
                }
        return Station(
                id = presenter.id,
                uri = uriTil.editText!!.text.toString(),
                title = titleTil.editText!!.text.toString(),
                group = folderTil.editText!!.text.toString(),
                genre = genres,
                url = urlTil.editText!!.text.toString(),
                sample = sampleTil.editText!!.text.toString().toInt(),
                bitrate = bitrateTil.editText!!.text.toString().toInt()
        )
    }

    private fun TextInputLayout.setTextWithoutAnimation(string: String) {
        isHintAnimationEnabled = false
        editText?.setText(string)
        isHintAnimationEnabled = true
    }

    private fun TextInputLayout.setEditable(enable: Boolean) {
        editText?.apply {
            isFocusable = enable
            isClickable = enable
            isFocusableInTouchMode = enable
            isCursorVisible = enable

            if (enable) setBackgroundResource(editTextBg)
            else setBackgroundResource(0)
        }
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

    private fun TextInputLayout.linkStyle(enable: Boolean) {
        editText?.apply {
            val string = text.toString()
            val color = ContextCompat.getColor(context, R.color.blue_500)
            if (enable) {
                val spannable = SpannableString(string)
                spannable.setSpan(URLSpan(string), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(ForegroundColorSpan(color), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setText(spannable)
            } else {
                setText(string)
            }
        }
    }
}


