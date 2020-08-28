package ru.luckycactus.steamroulette.presentation.utils.glide

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class RequestListenerAdapter<R>(
    val onResource: (R?) -> Unit
): RequestListener<R> {
    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<R>?,
        isFirstResource: Boolean
    ): Boolean {
        onResource(null)
        return false
    }

    override fun onResourceReady(
        resource: R,
        model: Any?,
        target: Target<R>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        onResource(resource)
        return false
    }
}