package io.github.vladimirmi.internetradioplayer.presentation.iconpicker

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.model.db.entity.Icon
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarBuilder
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarView
import io.github.vladimirmi.internetradioplayer.ui.base.BackPressListener
import io.github.vladimirmi.internetradioplayer.ui.base.BaseFragment
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
                colorPicker.setColor(presenter.icon.bg)
                colorPicker.setOnColorChangedListener(this::setBackgroundColor)

            } else if (checkedId == R.id.configForegroundBt) {
                colorPicker.setColor(presenter.icon.fg)
                colorPicker.setOnColorChangedListener(this::setForegroundColor)
            }
        }

        okBt.setOnClickListener { presenter.saveIcon() }
        cancelBt.setOnClickListener { presenter.exit() }
    }

    override fun onBackPressed() = presenter.onBackPressed()

    //region =============== IconPickerView ==============

    override fun buildToolbar(builder: ToolbarBuilder) {
        builder.build(activity as ToolbarView)
    }

    override fun setIcon(icon: Icon) {
        carousel.currentItem = icon.res
    }

    //endregion

    private fun setForegroundColor(colorInt: Int) {
        presenter.icon.fg = colorInt
        carousel.setFgColor(colorInt)
    }

    private fun setBackgroundColor(colorInt: Int) {
        presenter.icon.bg = colorInt
        carousel.setBgColor(colorInt)
    }
}
