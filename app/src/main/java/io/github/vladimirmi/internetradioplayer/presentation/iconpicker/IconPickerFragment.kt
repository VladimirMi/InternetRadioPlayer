package io.github.vladimirmi.internetradioplayer.presentation.iconpicker

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Icon
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.waitForMeasure
import io.github.vladimirmi.internetradioplayer.presentation.base.BackPressListener
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configurationsRg.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.configBackgroundBt) {
                colorPicker.setColor(presenter.currentIcon.bg)
                colorPicker.setOnColorChangedListener(carousel::setBgColor)

            } else {
                colorPicker.setColor(presenter.currentIcon.fg)
                colorPicker.setOnColorChangedListener(carousel::setFgColor)
            }
        }

        carousel.setIconChangeListener { presenter.currentIcon = it }

        okBt.setOnClickListener { presenter.saveIcon() }
        cancelBt.setOnClickListener { presenter.exit() }
    }

    override fun onBackPressed() = presenter.onBackPressed()

    //region =============== IconPickerView ==============

    override fun setIcon(icon: Icon) {
        carousel.waitForMeasure {
            carousel.setCurrentItem(icon.res, false)
            carousel.setBgColor(icon.bg)
            carousel.setFgColor(icon.fg)
        }

        if (configurationsRg.checkedRadioButtonId == -1) {
            configurationsRg.check(R.id.configForegroundBt)
        }
    }

    //endregion
}
