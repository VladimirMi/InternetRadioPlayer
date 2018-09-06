package io.github.vladimirmi.internetradioplayer.ui

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.vladimirmi.internetradioplayer.R
import kotlinx.android.synthetic.main.view_icon.view.*
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 05.09.2018.
 */

class CarouselPicker : ViewPager {

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context, null)

    init {
        with(CarouselAdapter()) {
            adapter = this
            offscreenPageLimit = count
        }
        clipChildren = false

        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                Timber.e("onPageSelected: $position")
            }

        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        pageMargin = (-measuredWidth / 1.5).toInt()
    }

}

class CarouselAdapter : PagerAdapter() {

    private val items = listOf(
            R.drawable.ic_station_1,
            R.drawable.ic_station_2,
            R.drawable.ic_station_3,
            R.drawable.ic_station_4
    )

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val iconView = LayoutInflater.from(container.context).inflate(R.layout.view_icon, null)
        container.addView(iconView)
        iconView.iconIv.setImageResource(items[position])
        iconView.tag = position
        iconView.setOnClickListener {
            (container as CarouselPicker).currentItem = position - 1
        }

        return iconView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
