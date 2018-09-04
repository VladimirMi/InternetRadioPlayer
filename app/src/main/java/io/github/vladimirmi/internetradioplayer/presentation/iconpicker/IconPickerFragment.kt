package io.github.vladimirmi.internetradioplayer.presentation.iconpicker

import `in`.goodiebag.carouselpicker.CarouselPicker
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.onTextChanges
import io.github.vladimirmi.internetradioplayer.extensions.setTint
import io.github.vladimirmi.internetradioplayer.extensions.setTintExt
import io.github.vladimirmi.internetradioplayer.model.entity.icon.IconOption
import io.github.vladimirmi.internetradioplayer.model.entity.icon.IconResource
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarBuilder
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarView
import io.github.vladimirmi.internetradioplayer.ui.base.BackPressListener
import io.github.vladimirmi.internetradioplayer.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_icon_picker.*
import kotlinx.android.synthetic.main.view_icon.*
import kotlinx.android.synthetic.main.view_icon_picker_content.*
import kotlinx.android.synthetic.main.view_station_icons.*
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
        iconTextEt.isSelected = true
        optionsRg.setOnCheckedChangeListener { _, checkedId ->
            presenter.iconOption = IconOption.fromId(checkedId)
        }

        configurationsRg.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.configBackgroundBt) {
                colorPicker.setColor(presenter.backgroundColor)
                colorPicker.setOnColorChangedListener {
                    presenter.backgroundColor = it
                }
            } else if (checkedId == R.id.configForegroundBt) {
                colorPicker.setColor(presenter.foregroundColor)
                colorPicker.setOnColorChangedListener {
                    presenter.foregroundColor = it
                }
            }
        }

        iconTextEt.onTextChanges { presenter.text = it }
        okBt.setOnClickListener { presenter.saveIcon(createIconBitmap()) }
        cancelBt.setOnClickListener { presenter.exit() }

        val imageItems = listOf(
                CarouselPicker.DrawableItem(R.drawable.ic_station_1),
                CarouselPicker.DrawableItem(R.drawable.ic_station_2),
                CarouselPicker.DrawableItem(R.drawable.ic_station_3),
                CarouselPicker.DrawableItem(R.drawable.ic_station_4)
        )
        val imageAdapter = CarouselPicker.CarouselViewAdapter(context, imageItems, 0)
        carousel.adapter = imageAdapter
    }

    override fun onBackPressed() = presenter.onBackPressed()

    //region =============== IconPickerView ==============

    override fun buildToolbar(builder: ToolbarBuilder) {
        builder.build(activity as ToolbarView)
    }

    override fun setIconImage(icon: Bitmap) {
        iconIv.setImageBitmap(icon)
    }

    override fun setIconText(text: String) {
        iconTv.text = text
        if (iconTextEt.text.toString() != text) iconTextEt.setText(text)
    }

    override fun setForegroundColor(colorInt: Int) {
        iconTv.setTextColor(colorInt)
        iconIv.background.mutate().setTintExt(colorInt)
        // todo for api 16, check others
        iconIv.invalidate()
        colorPicker.setColor(colorInt)
    }

    override fun setBackgroundColor(colorInt: Int) {
        iconFr.setTint(colorInt)
        colorPicker.setColor(colorInt)
    }

    override fun setOption(iconOption: IconOption) {
        optionsRg.check(iconOption.id)
        configurationsRg.check(R.id.configForegroundBt)
    }

    override fun setIconResource(iconResource: IconResource) {
        (iconsRg as RadioGroup).check(iconResource.id)
        iconIv.setBackgroundResource(iconResource.resId)
    }

    //endregion


    private fun createIconBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(iconFr.width, iconFr.height, Bitmap.Config.ARGB_8888)
        iconFr.draw(Canvas(bitmap))
        return bitmap
    }
}
