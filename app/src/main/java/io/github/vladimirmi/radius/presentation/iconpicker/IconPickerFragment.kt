package io.github.vladimirmi.radius.presentation.iconpicker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.onTextChanges
import io.github.vladimirmi.radius.extensions.setTint
import io.github.vladimirmi.radius.extensions.visible
import io.github.vladimirmi.radius.ui.base.BackPressListener
import io.github.vladimirmi.radius.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_icon_picker.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

class IconPickerFragment : BaseFragment(), IconPickerView, BackPressListener {

    override val layoutRes = R.layout.fragment_icon_picker

    @InjectPresenter
    lateinit var presenter: IconPickerPresenter

    @ProvidePresenter
    fun providePresenter(): IconPickerPresenter {
        val scope = Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
        return scope.getInstance(IconPickerPresenter::class.java).also {
            Toothpick.closeScope(this)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        iconTextEt.isSelected = true
        optionsRg.setOnCheckedChangeListener { _, checkedId ->
            presenter.checkedOption = when (checkedId) {
                R.id.optionServerUrlBt -> IconOption.SERVER
                R.id.optionStationUrlBt -> IconOption.STATION
                R.id.optionTextBt -> IconOption.TEXT
                else -> throw IllegalStateException()
            }
        }

        configurationsRg.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.configBackgroundBt) {
                colorPicker.setColor(presenter.backgroundColor)
                colorPicker.setOnColorChangedListener {
                    presenter.backgroundColor = it
                }
            } else if (checkedId == R.id.configTextBt) {
                colorPicker.setColor(presenter.textColor)
                colorPicker.setOnColorChangedListener {
                    presenter.textColor = it
                }
            }
        }

        iconTextEt.onTextChanges { presenter.text = it }
        okBt.setOnClickListener { presenter.saveIcon(createIconBitmap()) }
        cancelBt.setOnClickListener { presenter.exit() }
    }

    override fun onBackPressed() = presenter.onBackPressed()

    //region =============== IconPickerView ==============

    override fun setIconImage(icon: Bitmap) {
        iconIv.setImageBitmap(icon)
    }

    override fun setIconText(text: String) {
        iconTv.text = text
        if (iconTextEt.text.toString() != text) iconTextEt.setText(text)
    }

    override fun setIconTextColor(colorInt: Int) {
        iconTv.setTextColor(colorInt)
        colorPicker.setColor(colorInt)
    }

    override fun setBackgroundColor(colorInt: Int) {
        iconFr.setTint(colorInt)
        colorPicker.setColor(colorInt)
    }

    override fun hideStationUrlOption() {
        optionStationUrlBt.visible(false)
    }

    override fun hideTextOption() {
        optionTextBt.visible(false)
    }

    override fun setOption(iconOption: IconOption) {
        when (iconOption) {
            IconOption.SERVER -> {
                optionsRg.check(R.id.optionServerUrlBt)
                setupOption(false)
            }
            IconOption.STATION -> {
                optionsRg.check(R.id.optionStationUrlBt)
                setupOption(false)
            }
            IconOption.TEXT -> {
                optionsRg.check(R.id.optionTextBt)
                setupOption(true)
            }
            IconOption.DEFAULT -> setupOption(false)
        }
    }

    //endregion

    private fun setupOption(isText: Boolean) {
        configBackgroundBt.isChecked = true
        configTextBt.visible(isText)
        iconTv.visible(isText)
        iconIv.visible(!isText)
        iconTextEt.visible(isText)
    }

    private fun createIconBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(iconFr.width, iconFr.height, Bitmap.Config.ARGB_8888)
        iconFr.draw(Canvas(bitmap))
        return bitmap
    }
}