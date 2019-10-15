package ru.luckycactus.steamroulette.presentation.utils.glide

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.TransitionFactory

class DrawableNotCacheCrossFadeFactory(
    duration: Int = 300,
    isCrossFadeEnabled: Boolean = false
) : TransitionFactory<Drawable> {

    private val resourceTransition: DrawableCrossFadeTransition = DrawableCrossFadeTransition(
        duration,
        isCrossFadeEnabled
    )

    override fun build(
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Transition<Drawable> =
        if (dataSource == DataSource.MEMORY_CACHE || dataSource == DataSource.DATA_DISK_CACHE)
            NoTransition.get()
        else
            resourceTransition
}