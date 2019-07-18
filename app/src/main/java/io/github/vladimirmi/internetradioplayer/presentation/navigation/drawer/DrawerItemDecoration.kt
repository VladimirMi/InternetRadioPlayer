package io.github.vladimirmi.internetradioplayer.presentation.navigation.drawer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.extensions.dp
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 08.04.2019.
 */

class DrawerItemDecoration(context: Context) : DividerItemDecoration(context, RecyclerView.VERTICAL) {

    private var mDivider: Drawable? = null
    private val mBounds = Rect()
    private val divMargin = 8 * context.dp

    init {
        val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        mDivider = a.getDrawable(0)
        if (mDivider == null) {
            Timber.w("@android:attr/listDivider was not set in the theme used for this DividerItemDecoration. Please set that attribute all call setDrawable()")
        }
        a.recycle()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawVertical(c, parent)
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int
        val right: Int

        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right,
                    parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (isAboveDivider(parent, child)) {
                parent.getDecoratedBoundsWithMargins(child, mBounds)
                val bottom = mBounds.bottom + Math.round(child.translationY)
                val top = bottom - mDivider!!.intrinsicHeight
                mDivider!!.setBounds(left, top, right, bottom)
                mDivider!!.draw(canvas)
            }
        }
        canvas.restore()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        when {
            isAboveDivider(parent, view) -> outRect.set(0, 0, 0, divMargin)
            isBelowDivider(parent, view) -> outRect.set(0, divMargin, 0, 0)
            else -> outRect.set(0, 0, 0, 0)
        }
    }

    private fun isAboveDivider(parent: RecyclerView, view: View): Boolean {
        val adapter = parent.adapter as? DrawerAdapter
                ?: return false
        val position = parent.getChildAdapterPosition(view)
        if (position + 1 == adapter.itemCount) return false

        val cur = adapter.getItem(position)
        val next = adapter.getItem(position + 1)
        return cur.groupId != next.groupId
    }

    private fun isBelowDivider(parent: RecyclerView, view: View): Boolean {
        val adapter = parent.adapter as? DrawerAdapter
                ?: return false
        val position = parent.getChildAdapterPosition(view)
        if (position < 1) return false

        val cur = adapter.getItem(position)
        val prev = adapter.getItem(position - 1)
        return cur.groupId != prev.groupId
    }

}