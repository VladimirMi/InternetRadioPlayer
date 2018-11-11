package io.github.vladimirmi.internetradioplayer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.ViewPager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.ICONS
import io.github.vladimirmi.internetradioplayer.data.db.entity.Icon
import io.github.vladimirmi.internetradioplayer.extensions.color
import io.github.vladimirmi.internetradioplayer.extensions.setTintExt
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
    private var pageTransformer: CustomPageTransformer
    private var iconListener: ((Icon) -> Unit)? = null

    init {
        val adapter = CarouselAdapter()
        this.adapter = adapter
        offscreenPageLimit = adapter.count
        overScrollMode = OVER_SCROLL_NEVER
        pageTransformer = CustomPageTransformer()
        setPageTransformer(false, pageTransformer)

        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                iconListener?.invoke(Icon(position, bg, fg))
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

    fun setIconChangeListener(listener: (Icon) -> Unit) {
        iconListener = listener
    }

    fun setBgColor(colorInt: Int) {
        bg = colorInt
        getImageView(currentItem)?.background?.setTintExt(colorInt)
        iconListener?.invoke(Icon(currentItem, bg, fg))
    }

    fun setFgColor(colorInt: Int) {
        fg = colorInt
        getImageView(currentItem)?.drawable?.setTintExt(colorInt)
        iconListener?.invoke(Icon(currentItem, bg, fg))
    }

    private fun getImageView(position: Int): ImageView? {
        return (getChildAt(position) as? ViewGroup)?.getChildAt(0) as? ImageView
    }

    inner class CustomPageTransformer : androidx.viewpager.widget.ViewPager.PageTransformer {

        override fun transformPage(page: View, position: Float) {
            val imageView = page.iconIv
            val absPosition = Math.abs(position)

            fun compareAndSetColors(bg: Int, fg: Int) {
                imageView.background.setTintExt(bg)
                imageView.drawable.setTintExt(fg)
            }

            if (absPosition < 0.16666) {
                compareAndSetColors(bg, fg)
            } else {
                compareAndSetColors(defaultBg, defaultFg)
            }

            imageView.scaleY = 1 - absPosition
            imageView.scaleX = 1 - absPosition
        }
    }
}

class CarouselAdapter : androidx.viewpager.widget.PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return ICONS.size
    }

    @SuppressLint("InflateParams")
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
