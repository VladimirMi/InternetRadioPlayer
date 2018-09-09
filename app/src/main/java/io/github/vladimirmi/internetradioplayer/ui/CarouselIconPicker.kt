package io.github.vladimirmi.internetradioplayer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.setFgTint
import io.github.vladimirmi.internetradioplayer.model.db.entity.ICONS
import kotlinx.android.synthetic.main.view_icon.view.*

/**
 * Created by Vladimir Mikhalev 05.09.2018.
 */

class CarouselIconPicker : ViewPager {

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context, null)

    private val defaultBg = context.color(R.color.grey_50)
    private val defaultFg = context.color(R.color.grey_600)
    private var bg = defaultBg
    private var fg = defaultFg

    init {
        val adapter = CarouselAdapter()
        this.adapter = adapter
        offscreenPageLimit = 5
        clipChildren = true
        overScrollMode = OVER_SCROLL_NEVER
        setPageTransformer(false, CustomPageTransformer())

        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (positionOffset > 0.5) {
                    setBgColor(defaultBg, position)
                    setFgColor(defaultFg, position)
                    setBgColor(bg, position + 1)
                    setFgColor(fg, position + 1)

                } else if (positionOffset < 0.5) {
                    setBgColor(defaultBg, position + 1)
                    setFgColor(defaultFg, position + 1)
                    setBgColor(bg, position)
                    setFgColor(fg, position)
                }
            }

            override fun onPageSelected(position: Int) {

            }
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        pageMargin = (-measuredWidth / 1.5).toInt()
    }


    private var startClick = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            startClick = ev.x
        } else if (ev.action == MotionEvent.ACTION_UP && startClick == ev.x) {
            if (ev.x < width / 3) {
                setCurrentItem(currentItem - 1, false)
                return true
            } else if (ev.x > width / 3 * 2) {
                setCurrentItem(currentItem + 1, false)
                return true
            }
        }
        return super.onTouchEvent(ev)
    }

    fun setFgColor(colorInt: Int) {
        fg = colorInt
        setFgColor(colorInt, currentItem)
    }

    fun setBgColor(colorInt: Int) {
        bg = colorInt
        setBgColor(colorInt, currentItem)
    }

    private fun setFgColor(colorInt: Int, position: Int) {
        getImageView(position)?.setFgTint(colorInt)
    }

    private fun setBgColor(colorInt: Int, position: Int) {
        getImageView(position)?.setBackgroundColor(colorInt)
    }

    private fun getImageView(position: Int): ImageView? {
        return if (position < 0 || position >= childCount) null
        else (getChildAt(position) as ViewGroup).getChildAt(0) as ImageView
    }

}

class CarouselAdapter : PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return ICONS.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val iconView = LayoutInflater.from(container.context).inflate(R.layout.view_icon, null)
        container.addView(iconView)
        iconView.iconIv.setImageResource(ICONS[position])
        iconView.tag = position

        return iconView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}

class CustomPageTransformer : ViewPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        page.iconIv.scaleY = 1 - Math.abs(position)
        page.iconIv.scaleX = 1 - Math.abs(position)
    }
}
