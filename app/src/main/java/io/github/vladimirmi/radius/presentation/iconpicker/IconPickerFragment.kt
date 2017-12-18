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
        optionsRg.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.optionStationUrlBt, R.id.optionServerUrlBt -> presenter.option(true, false, false)
                R.id.optionNameBt -> presenter.option(false, true, false)
                R.id.optionAddBt -> presenter.option(false, false, true)
            }
        }

        configurationsRg.setOnCheckedChangeListener { group, checkedId ->
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

        iconTextEt.onTextChanges { presenter.setText(it) }
        okBt.setOnClickListener { presenter.saveIcon(createIcon()) }
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
        optionNameBt.visible(false)
    }

    override fun option(url: Boolean, name: Boolean, add: Boolean) {
        configBackgroundBt.isChecked = true
        configSelectBt.visible(add)
        configTextBt.visible(name)
        iconTv.visible(name)
        iconIv.visible(!name)
        iconTextEt.visible(name)
    }

    //endregion

    private fun createIcon(): Bitmap {
        val bitmap = Bitmap.createBitmap(iconFr.width, iconFr.height, Bitmap.Config.ARGB_8888)
        iconFr.draw(Canvas(bitmap))
        return bitmap
    }
}