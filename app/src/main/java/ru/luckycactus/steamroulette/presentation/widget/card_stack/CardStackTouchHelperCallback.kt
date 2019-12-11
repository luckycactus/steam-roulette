package ru.luckycactus.steamroulette.presentation.widget.card_stack

import android.graphics.Canvas
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import ru.luckycactus.steamroulette.presentation.widget.touchhelper.ItemTouchHelper

class CardStackTouchHelperCallback(
    private val onSwiped: () -> Unit,
    private val onSwipedLeft: (() -> Unit)?,
    private val onSwipedRight: (() -> Unit)?,
    private val onSwipeProgress: ((Float, Float) -> Unit)?
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int = if (viewHolder.adapterPosition == 0) {
        //doesn't work properly for some reason
        makeFlag(
            ItemTouchHelper.ACTION_STATE_IDLE,
            ItemTouchHelper.LEFT or ItemTouchHelper.UP or
                    ItemTouchHelper.RIGHT or ItemTouchHelper.DOWN
        ) or
                makeFlag(
                    ItemTouchHelper.ACTION_STATE_SWIPE,
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                )
    } else {
        makeMovementFlags(0, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when (direction) {
            ItemTouchHelper.LEFT -> onSwipedLeft?.invoke()
            ItemTouchHelper.RIGHT -> onSwipedRight?.invoke()
        }
        onSwiped()
    }

    override fun getAnimationDuration(
        recyclerView: RecyclerView,
        animationType: Int,
        animateDx: Float,
        animateDy: Float
    ): Long = 300L

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        if (viewHolder.adapterPosition == 0) {
            val swipeProgress = (dX / recyclerView.width).coerceIn(-1f, 1f)
            viewHolder.itemView.rotation = 15 * swipeProgress
            onSwipeProgress?.invoke(swipeProgress, getSwipeThreshold(viewHolder))
            if (viewHolder is ViewHolderSwipeProgressListener) {
                viewHolder.onSwipeProgress(swipeProgress, getSwipeThreshold(viewHolder))
            }

            if (recyclerView.childCount == 1) return

            val layoutManager = recyclerView.layoutManager as CardStackLayoutManager
            for (i in 0 until recyclerView.childCount) {
                val vh = recyclerView.getChildViewHolder(recyclerView.getChildAt(i))
                if (vh.adapterPosition != 0) {
                    layoutManager.setChildGaps(
                        recyclerView.getChildAt(i),
                        vh.adapterPosition,
                        swipeProgress
                    )
                }
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.rotation = 0f
        viewHolder.itemView.scaleX = 1f
        viewHolder.itemView.scaleY = 1f
        if (viewHolder is ViewHolderSwipeProgressListener) {
            viewHolder.onSwipeProgress(0f, getSwipeThreshold(viewHolder))
        }
    }

    interface ViewHolderSwipeProgressListener {
        fun onSwipeProgress(progress: Float, threshold: Float)
    }
}