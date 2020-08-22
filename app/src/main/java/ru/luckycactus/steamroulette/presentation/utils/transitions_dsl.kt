package ru.luckycactus.steamroulette.presentation.utils

import android.view.Gravity
import androidx.transition.*

class TransitionSetBuilder : TransitionSet()

const val DEFAULT_TRANSITION_DURATION = 375L

inline fun transitionSet(block: TransitionSetBuilder.() -> Unit) =
    TransitionSetBuilder().apply {
        duration = DEFAULT_TRANSITION_DURATION
        block()
    }

inline fun TransitionSetBuilder.fade(block: Fade.() -> Unit = {}): Fade =
    Fade().also {
        addTransition(it)
        it.block()
    }

inline fun TransitionSetBuilder.slide(slideEdge: Int = Gravity.BOTTOM, block: Slide.() -> Unit = {}): Slide =
    Slide(slideEdge).also {
        addTransition(it)
        it.block()
    }

inline fun TransitionSetBuilder.changeBounds(block: ChangeBounds.() -> Unit = {}): ChangeBounds =
    ChangeBounds().also {
        addTransition(it)
        it.block()
    }

inline fun TransitionSetBuilder.changeClipBounds(block: ChangeClipBounds.() -> Unit = {}): ChangeClipBounds =
    ChangeClipBounds().also {
        addTransition(it)
        it.block()
    }

inline fun TransitionSetBuilder.changeTransform(block: ChangeTransform.() -> Unit = {}): ChangeTransform =
    ChangeTransform().also {
        addTransition(it)
        it.block()
    }

fun Transition.listener(
    onTransitionEnd: (Transition) -> Unit = {},
    onTransitionResume: (Transition) -> Unit = {},
    onTransitionPause: (Transition) -> Unit = {},
    onTransitionCancel: (Transition) -> Unit = {},
    onTransitionStart: (Transition) -> Unit = {}
): Transition.TransitionListener = object : Transition.TransitionListener {
    override fun onTransitionEnd(transition: Transition) {
        onTransitionEnd(transition)
    }

    override fun onTransitionResume(transition: Transition) {
        onTransitionResume(transition)
    }

    override fun onTransitionPause(transition: Transition) {
        onTransitionPause(transition)
    }

    override fun onTransitionCancel(transition: Transition) {
        onTransitionCancel(transition)
    }

    override fun onTransitionStart(transition: Transition) {
        onTransitionStart(transition)
    }
}.also {
    addListener(it)
}