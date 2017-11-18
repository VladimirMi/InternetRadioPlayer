package io.github.vladimirmi.radius.ui.media_list

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.extensions.dp
import io.github.vladimirmi.radius.extensions.sp

/**
 * Created by Vladimir Mikhalev 17.11.2017.
 */

abstract class ItemSwipeCallback(context: Context,
                                 dragDirs: Int,
                                 swipeDirs: Int)
    : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    //Settings
    companion object {
        private const val RIGHT_ICON = R.drawable.ic_delete
        private const val LEFT_ICON = R.drawable.ic_edit
        private const val RIGHT_COLOR = R.color.deep_orange_500
        private const val LEFT_COLOR = R.color.secondaryDarkColor
        private const val RIGHT_TEXT = R.string.remove
        private const val LEFT_TEXT = R.string.show
        private const val TEXT_SIZE_SP = 15f
        private const val ICON_SIZE_DP = 24f
        private const val HORIZONTAL_MARGIN_ICON_DP = 16f
        private const val HORIZONTAL_MARGIN_TEXT_DP = 48f
        private const val VERTICAL_OFFSET_TEXT_DP = 1f
    }

    private val dp = context.dp
    private val sp = context.sp
    private val rightIcon = ContextCompat.getDrawable(context, RIGHT_ICON)
    private val leftIcon = ContextCompat.getDrawable(context, LEFT_ICON)
    private val rightColor = ContextCompat.getColor(context, RIGHT_COLOR)
    private val leftColor = ContextCompat.getColor(context, LEFT_COLOR)
    private val rightText = context.getString(RIGHT_TEXT).toUpperCase()
    private val leftText = context.getString(LEFT_TEXT).toUpperCase()

    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ): Boolean = false

    abstract override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)

    override fun getSwipeDirs(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
        if (viewHolder is MediaGroupTitleVH) return 0
        return super.getSwipeDirs(recyclerView, viewHolder)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

            val horizontalTextMarginPx = HORIZONTAL_MARGIN_TEXT_DP * dp
            val offsetY = VERTICAL_OFFSET_TEXT_DP * dp
            val textSizePx = TEXT_SIZE_SP * sp
            val textPaint = Paint()
            textPaint.color = Color.WHITE
            textPaint.textSize = textSizePx
            textPaint.isAntiAlias = true


            val itemView = viewHolder.itemView
            val itemHeightPx = itemView.bottom.toFloat() - itemView.top.toFloat()
            val horizontalIconMarginPx = HORIZONTAL_MARGIN_ICON_DP * dp
            val iconSizePx = ICON_SIZE_DP * dp

            if (dX > 0) {
                //swipe to right

                //drawing splash_background
                val leftBound = itemView.left.toFloat()
                val topBound = itemView.top.toFloat()
                val bottomBound = itemView.bottom.toFloat()
                val clipShape = RectF(leftBound, topBound, dX, bottomBound)
                c.clipRect(clipShape)
                c.drawColor(leftColor)

                //drawing text
                textPaint.textAlign = Paint.Align.LEFT
                val textX = (itemView.left + horizontalTextMarginPx).toInt()
                val textY = (itemView.top.toFloat() + itemHeightPx / 2 + textSizePx / 2 - offsetY).toInt()
                c.drawText(leftText, textX.toFloat(), textY.toFloat(), textPaint)

                //drawing icon
                val iconLeft = (itemView.left + horizontalIconMarginPx).toInt()
                val iconTop = (itemView.top + itemHeightPx / 2 - iconSizePx / 2).toInt()
                val iconRight = (itemView.left.toFloat() + horizontalIconMarginPx + iconSizePx).toInt()
                val iconBottom = (itemView.bottom - itemHeightPx / 2 + iconSizePx / 2).toInt()
                leftIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                leftIcon.draw(c)

            } else {
                //swipe to left

                //drawing splash_background
                val leftBound = itemView.right.toFloat() + dX
                val topBound = itemView.top.toFloat()
                val rightBound = itemView.right.toFloat()
                val bottomBound = itemView.bottom.toFloat()
                val clipShape = RectF(leftBound, topBound, rightBound, bottomBound)
                c.clipRect(clipShape)
                c.drawColor(rightColor)

                //drawing text
                textPaint.textAlign = Paint.Align.RIGHT
                val textX = (itemView.right - horizontalTextMarginPx).toInt()
                val textY = (itemView.top.toFloat() + itemHeightPx / 2 + textSizePx / 2 - offsetY).toInt()
                c.drawText(rightText, textX.toFloat(), textY.toFloat(), textPaint)

                //drawing icon
                val iconLeft = (itemView.right.toFloat() - horizontalIconMarginPx - iconSizePx).toInt()
                val iconTop = (itemView.top + itemHeightPx / 2 - iconSizePx / 2).toInt()
                val iconRight = (itemView.right - horizontalIconMarginPx).toInt()
                val iconBottom = (itemView.bottom - itemHeightPx / 2 + iconSizePx / 2).toInt()
                rightIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                rightIcon.draw(c)
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}