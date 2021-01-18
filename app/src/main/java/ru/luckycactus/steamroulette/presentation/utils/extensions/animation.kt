package ru.luckycactus.steamroulette.presentation.utils.extensions

import android.view.animation.Animation

inline fun Animation.listener(
    crossinline onEnd: (Animation) -> Unit = {},
    crossinline onStart: (Animation) -> Unit = {},
    crossinline onRepeat: (Animation) -> Unit = {},
) = object : Animation.AnimationListener {

    override fun onAnimationStart(animation: Animation) {
        onStart(animation)
    }

    override fun onAnimationEnd(animation: Animation) {
        onEnd(animation)
    }

    override fun onAnimationRepeat(animation: Animation) {
        onRepeat(animation)
    }
}.also { setAnimationListener(it) }