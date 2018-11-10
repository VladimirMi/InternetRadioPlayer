package io.github.vladimirmi.internetradioplayer.presentation.iconpicker

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.presentation.base.BackPressListener
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragmentLegacy
import kotlinx.android.synthetic.main.fragment_icon_picker.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

class IconPickerFragment : BaseFragmentLegacy(), IconPickerView, BackPressListener {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupCarousel()
        setupColorPicker(configurationsRg.checkedRadioButtonId)
        configurationsRg.setOnCheckedChangeListener { _, checkedId ->
            setupColorPicker(checkedId)
        }

        okBt.setOnClickListener { presenter.saveIcon() }
        cancelBt.setOnClickListener { presenter.exit() }
    }

    private fun setupColorPicker(checkedId: Int) {
        if (checkedId == R.id.configBackgroundBt) {
            colorPicker.setColor(presenter.currentIcon.bg)
            colorPicker.setOnColorChangedListener(carousel::setBgColor)

        } else {
            colorPicker.setColor(presenter.currentIcon.fg)
            colorPicker.setOnColorChangedListener(carousel::setFgColor)
        }
    }

    private fun setupCarousel() {
        carousel.setCurrentItem(presenter.currentIcon.res, false)
        carousel.setBgColor(presenter.currentIcon.bg)
        carousel.setFgColor(presenter.currentIcon.fg)
        carousel.setIconChangeListener { presenter.currentIcon = it }
    }

    override fun onBackPressed() = presenter.onBackPressed()
}
