package ru.luckycactus.steamroulette.presentation.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceDecoration(
    private val startEndSpace: Int,
    private val sideSpace: Int,
    private val innerSpace: Int,
    private val vertical: Boolean = true
) : RecyclerView.ItemDecoration() {

    constructor(
        margin: Int,
        vertical: Boolean = true
    ) : this(margin, margin, margin, vertical)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val adapterPosition = parent.getChildAdapterPosition(view)
        val firstItem = adapterPosition == 0
        val lastItem = adapterPosition == parent.adapter!!.itemCount - 1
        with(outRect) {
            if (vertical) {
                if (firstItem) {
                    top = startEndSpace
                }
                left = sideSpace
                right = sideSpace
                bottom = if (lastItem) {
                    startEndSpace
                } else {
                    innerSpace
                }
            } else {
                if (firstItem) {
                    left = startEndSpace
                }
                top = sideSpace
                bottom = sideSpace
                right = if (lastItem) {
                    startEndSpace
                } else {
                    innerSpace
                }
            }
        }
    }
}