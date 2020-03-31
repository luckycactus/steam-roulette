package ru.luckycactus.steamroulette.presentation.ui

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView

class SimpleIdItemKeyProvider(
    private val recyclerView: RecyclerView
) : ItemKeyProvider<Long>(SCOPE_MAPPED) {

    override fun getKey(position: Int): Long? {
        return recyclerView.adapter?.getItemId(position)
            ?: throw IllegalStateException("RecyclerView adapter is not set!")
    }

    override fun getPosition(key: Long): Int {
        return recyclerView.findViewHolderForItemId(key)?.adapterPosition
            ?: RecyclerView.NO_POSITION
    }
}