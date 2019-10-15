package ru.luckycactus.steamroulette.presentation.utils.glide

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.TransitionFactory

class DrawableAlwaysCrossFadeFactory(
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
    ): Transition<Drawable> = resourceTransition
}