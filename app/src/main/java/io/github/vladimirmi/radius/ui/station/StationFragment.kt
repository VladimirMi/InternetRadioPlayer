package io.github.vladimirmi.radius.ui.station

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.presentation.station.StationPresenter
import io.github.vladimirmi.radius.presentation.station.StationView
import io.github.vladimirmi.radius.ui.TagView
import io.github.vladimirmi.radius.ui.base.BaseFragment
import io.github.vladimirmi.radius.ui.root.RootActivity
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

    @InjectPresenter lateinit var presenter: StationPresenter
    private var editTextBg: Int = 0

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

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val typedValue = TypedValue()

        activity.theme.resolveAttribute(android.R.attr.editTextBackground, typedValue, true)
        editTextBg = typedValue.resourceId
    }

    override fun setStation(station: Station) {
        title.setTextWithoutAnimation(station.title)
        if (station.group.isEmpty()) group.remove()
        else group.setTextWithoutAnimation(station.group)
        station.url?.let { url.setTextWithoutAnimation(it) } ?: url.remove()
        station.bitrate?.toString()?.let { bitrate.setTextWithoutAnimation(it + "kbps") }
                ?: bitrate.setTextWithoutAnimation("n/a")
        station.source?.toString()?.let { sample.setTextWithoutAnimation(it + "Hz") }
                ?: sample.setTextWithoutAnimation("n/a")

        station.genre.forEach { flex_box.addView(TagView(context, it, null)) }
    }


    override fun buildToolbar(builder: ToolbarBuilder) {
        builder.build(activity as RootActivity)
    }

    override fun setEditable(enable: Boolean) {
        title.editText?.setEditable(enable)
        group.editText?.setEditable(enable)
        url.editText?.setEditable(enable)
        bitrate.editText?.setEditable(enable)
        sample.editText?.setEditable(enable)
        if (enable) group.show() else group.remove()
    }

    private fun TextInputLayout.setTextWithoutAnimation(string: String) {
        isHintAnimationEnabled = false
        editText?.setText(string)
        isHintAnimationEnabled = true
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

//todo extract
fun View.remove() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}


