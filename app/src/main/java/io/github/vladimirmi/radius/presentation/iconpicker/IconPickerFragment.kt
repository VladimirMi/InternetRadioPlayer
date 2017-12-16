package io.github.vladimirmi.radius.presentation.iconpicker

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.color
import io.github.vladimirmi.radius.extensions.setTint
import io.github.vladimirmi.radius.extensions.visible
import io.github.vladimirmi.radius.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_icon_picker.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

class IconPickerFragment : BaseFragment(), IconPickerView {

    override val layoutRes = R.layout.fragment_icon_picker

    @InjectPresenter
    lateinit var presenter: IconPickerPresenter

    @ProvidePresenter
    fun providePresenter(): IconPickerPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(IconPickerPresenter::class.java).also {
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

        colorPicker.initColor(context.color(R.color.accentColor))
        configurationsRg.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.configBackgroundBt) {
                colorPicker.setOnColorChangedListener {
                    presenter.setBackgroundColor(it)
                }
            } else if (checkedId == R.id.configTextBt) {
                colorPicker.setOnColorChangedListener {
                    presenter.setTextColor(it)
                }
            }
        }
    }

    //region =============== IconPickerView ==============

    override fun setIconImage(icon: Bitmap) {
        iconIv.setImageBitmap(icon)
    }

    override fun setBackgroundColor(colorInt: Int) {
        iconFr.setTint(colorInt)
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
}