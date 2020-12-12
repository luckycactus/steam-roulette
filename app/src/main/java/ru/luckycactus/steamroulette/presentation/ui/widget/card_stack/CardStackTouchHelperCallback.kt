package ru.luckycactus.steamroulette.presentation.ui.widget.card_stack

import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import ru.luckycactus.steamroulette.presentation.ui.widget.touchhelper.ItemTouchHelper

class CardStackTouchHelperCallback(
    private val onSwiped: () -> Unit,
    private val onSwipedLeft: (() -> Unit)?,
    private val onSwipedRight: (() -> Unit)?,
    private val onSwipeProgress: ((Float, Float) -> Unit)?
) : ItemTouchHelper.Callback() {

    private var inSwipeState = false
    private var swipeProgress = 0f

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int = if (viewHolder.bindingAdapterPosition == 0) {
        makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
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
        onSwipeProgress?.invoke(0f, getSwipeThreshold(viewHolder))
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
        if (viewHolder.bindingAdapterPosition == 0) {
            val newSwipeProgress = (dX / recyclerView.width).coerceIn(-1f, 1f)
            val lastSwipeState = this.inSwipeState
            inSwipeState = (newSwipeProgress != 0f)
            val swipeStateChanged = this.inSwipeState != lastSwipeState

            viewHolder.itemView.rotation = 15 * newSwipeProgress
            if (swipeProgress != newSwipeProgress) {
                onSwipeProgress?.invoke(newSwipeProgress, getSwipeThreshold(viewHolder))
                swipeProgress = newSwipeProgress
            }
            if (viewHolder is ViewHolderSwipeProgressListener) {
                viewHolder.onSwipeProgress(newSwipeProgress, getSwipeThreshold(viewHolder))
            }

            if (recyclerView.childCount == 1) return

            val layoutManager = recyclerView.layoutManager as CardStackLayoutManager
            for (i in 0 until recyclerView.childCount) {
                val vh = recyclerView.getChildViewHolder(recyclerView.getChildAt(i))
                val adapterPosition = vh.bindingAdapterPosition
                if (adapterPosition != 0) {
                    layoutManager.setChildGaps(
                        recyclerView.getChildAt(i),
                        adapterPosition,
                        newSwipeProgress
                    )
                }
                if (swipeStateChanged && (adapterPosition == 1) && vh is ViewHolderVisibleHintListener) {
                    vh.setVisibleHint(inSwipeState)
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
        if (viewHolder.bindingAdapterPosition < 0 && viewHolder is ViewHolderVisibleHintListener) {
            viewHolder.setVisibleHint(false)
            this.inSwipeState = false
        }
    }

    interface ViewHolderSwipeProgressListener {
        fun onSwipeProgress(progress: Float, threshold: Float)
    }

    interface ViewHolderVisibleHintListener {
        fun setVisibleHint(visible: Boolean)
    }
}