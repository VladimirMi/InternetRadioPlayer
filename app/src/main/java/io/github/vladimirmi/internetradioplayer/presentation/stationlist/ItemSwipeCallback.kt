package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.dp
import io.github.vladimirmi.internetradioplayer.extensions.sp

/**
 * Created by Vladimir Mikhalev 17.11.2017.
 */

abstract class ItemSwipeCallback(context: Context) : ItemTouchHelper.SimpleCallback(0, 0) {

    private val dp = context.dp
    private val sp = context.sp
    //    private val rightIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
    private val leftIcon = ContextCompat.getDrawable(context, R.drawable.ic_edit)!!
    //    private val rightColor = ContextCompat.getColor(context, R.color.deep_orange_500)
    private val leftColor = ContextCompat.getColor(context, R.color.accentColor)
    //    private val rightText = context.getString(R.string.menu_station_delete).toUpperCase()
    private val leftText = context.getString(R.string.station_list_details).toUpperCase()
    private val rightShadow = ContextCompat.getDrawable(context, R.drawable.shadow_swipe)!!
    private val leftShadow = ContextCompat.getDrawable(context, R.drawable.shadow_swipe_reverse)!!

    private val horizontalTextMargin = 48 * dp
    private val textSize = 14 * sp
    private val horizontalIconMargin = 16 * dp
    private val iconSize = 24 * dp

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = this@ItemSwipeCallback.textSize.toFloat()
        isAntiAlias = true
    }

    abstract override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                 target: RecyclerView.ViewHolder): Boolean

    abstract override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
            onStartDrag(viewHolder.adapterPosition)

        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) onIdle()
        super.onSelectedChanged(viewHolder, actionState)
    }

    abstract fun onStartDrag(position: Int)

    abstract fun onIdle()

    override fun getDragDirs(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
        return ItemTouchHelper.UP or ItemTouchHelper.DOWN
    }

    override fun getSwipeDirs(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
        if (viewHolder is GroupTitleVH) return 0
        return ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    }

    override fun onChildDraw(c: Canvas,
                             recyclerView: RecyclerView,
                             viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float,
                             actionState: Int,
                             isCurrentlyActive: Boolean) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top

            if (dX > 0) {
                //swipe to right

                //drawing splash_background
                val splashL = itemView.left.toFloat()
                val splashT = itemView.top.toFloat()
                val splashB = itemView.bottom.toFloat()
                val clipShape = RectF(splashL, splashT, dX, splashB)
                c.save()
                c.clipRect(clipShape)
                c.drawColor(leftColor)
                c.restore()

                //drawing text
                textPaint.textAlign = Paint.Align.LEFT
                val textX = itemView.left + horizontalTextMargin
                val textY = itemView.top + itemHeight / 2 + textSize / 2 - 1 * dp
                c.drawText(leftText, textX.toFloat(), textY.toFloat(), textPaint)

                //drawing icon
                val iconL = itemView.left + horizontalIconMargin
                val iconT = itemView.top + itemHeight / 2 - iconSize / 2
                val iconR = itemView.left + horizontalIconMargin + iconSize
                val iconBottom = itemView.bottom - itemHeight / 2 + iconSize / 2
                leftIcon.setBounds(iconL, iconT, iconR, iconBottom)
                leftIcon.draw(c)


                //drawing shadow
                val shadowL = dX.toInt() - leftShadow.minimumWidth
                val shadowT = itemView.top
                val shadowR = dX.toInt()
                val shadowB = itemView.bottom
                leftShadow.setBounds(shadowL, shadowT, shadowR, shadowB)
                leftShadow.draw(c)

            } else {
                //swipe to left

                //drawing splash_background
                val splashL = itemView.right.toFloat() + dX
                val splashT = itemView.top.toFloat()
                val splashR = itemView.right.toFloat()
                val splashB = itemView.bottom.toFloat()
                val clipShape = RectF(splashL, splashT, splashR, splashB)
                c.save()
                c.clipRect(clipShape)
                c.drawColor(leftColor)
                c.restore()

                //drawing text
                textPaint.textAlign = Paint.Align.RIGHT
                val textX = itemView.right - horizontalTextMargin
                val textY = itemView.top + itemHeight / 2 + textSize / 2 - 1 * dp
                c.drawText(leftText, textX.toFloat(), textY.toFloat(), textPaint)

                //drawing icon
                val iconL = itemView.right - horizontalIconMargin - iconSize
                val iconT = itemView.top + itemHeight / 2 - iconSize / 2
                val iconR = itemView.right - horizontalIconMargin
                val iconB = itemView.bottom - itemHeight / 2 + iconSize / 2
                leftIcon.setBounds(iconL, iconT, iconR, iconB)
                leftIcon.draw(c)

                //drawing shadow
                val shadowL = itemView.right + dX.toInt()
                val shadowT = itemView.top
                val shadowR = itemView.right + dX.toInt() + rightShadow.minimumWidth
                val shadowB = itemView.bottom
                rightShadow.setBounds(shadowL, shadowT, shadowR, shadowB)
                rightShadow.draw(c)
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
