package ru.luckycactus.steamroulette.presentation.widget.card_stack

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.absoluteValue

class CardStackLayoutManager : RecyclerView.LayoutManager() {

    var maxChildrenCount: Int = 5
        set(value) {
            field = maxOf(1, value)
        }

    var scaleGap: Float = 0.1f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }


    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
        RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)

        val childCount = minOf(maxChildrenCount, state.itemCount)
        for (i in childCount - 1 downTo 0) {
            val v = recycler.getViewForPosition(i)
            addView(v)
            measureChildWithMargins(v, 0, 0)
            layoutDecorated(
                v,
                paddingLeft,
                paddingTop,
                width - paddingRight,
                height - paddingBottom
            )
            setChildGaps(v, i, 0f)
        }
    }

    fun setChildGaps(view: View, layer: Int, swipeProgress: Float) {
        var scale = 1f
        if (layer > 0)
            scale -= (layer - swipeProgress.absoluteValue) * scaleGap
        view.scaleX = scale
        view.scaleY = scale
    }
}