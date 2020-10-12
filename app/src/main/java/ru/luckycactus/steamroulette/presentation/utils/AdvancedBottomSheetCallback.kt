package ru.luckycactus.steamroulette.presentation.utils

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

/**
 * Adapter for BottomSheetBehavior.BottomSheetCallback,
 * which additionally tracks previous state and changes between slideOffsets
 * Warning: value of diff on first call of onSlide is 0 because
 * we don't have any way to get current value from behavior
 * @param state we should set this to current state
 */
abstract class AdvancedBottomSheetCallback(
    private var state: Int = BottomSheetBehavior.STATE_COLLAPSED
) : BottomSheetBehavior.BottomSheetCallback() {

    private var lastSlide = 0f

    final override fun onSlide(bottomSheet: View, slideOffset: Float) {
        val diff = slideOffset - lastSlide
        lastSlide = slideOffset
        onSlide(bottomSheet, slideOffset, diff, state)
    }

    final override fun onStateChanged(bottomSheet: View, newState: Int) {
        onStateChanged(bottomSheet, newState, state)
        state = newState
    }

    fun setState(state: Int) {
        this.state = state
    }

    open fun onSlide(bottomSheet: View, slideOffset: Float, diff: Float, state: Int) {}

    open fun onStateChanged(bottomSheet: View, newState: Int, previousState: Int) {}
}