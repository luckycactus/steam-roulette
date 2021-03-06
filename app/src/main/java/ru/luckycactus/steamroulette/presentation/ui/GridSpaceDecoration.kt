package ru.luckycactus.steamroulette.presentation.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpaceDecoration(
    var spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean = false
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        var position = parent.getChildAdapterPosition(view) // item position
        if (position < 0) // item is removing from adapter
            position = parent.getChildLayoutPosition(view)
        val column = position % spanCount // item column
        if (includeEdge) {
            outRect.left =
                spacing - column * spacing / spanCount // innerSpace - column * ((1f / columnCount) * innerSpace)
            outRect.right =
                (column + 1) * spacing / spanCount // (column + 1) * ((1f / columnCount) * innerSpace)
            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            outRect.left =
                column * spacing / spanCount // column * ((1f / columnCount) * innerSpace)
            outRect.right =
                spacing - (column + 1) * spacing / spanCount // innerSpace - (column + 1) * ((1f /    columnCount) * innerSpace)
            if (position >= spanCount) {
                outRect.top = spacing // item top
            }
        }
    }
}