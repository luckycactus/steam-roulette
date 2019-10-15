package ru.luckycactus.steamroulette.presentation.utils.glide

import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.TransitionFactory

class DrawableAlwaysCrossFadeFactory(
    duration: Int
) : TransitionFactory<Drawable> {

    private val resourceTransition: DrawableCrossFadeTransition = DrawableCrossFadeTransition(
        duration,
        false
    )

    override fun build(
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Transition<Drawable> {
        Log.d("ololo", dataSource.toString() + " " + isFirstResource.toString())
        return if (dataSource == DataSource.MEMORY_CACHE && !isFirstResource)
            NoTransition.get()
        else
            resourceTransition
        //return resourceTransition
    }
}