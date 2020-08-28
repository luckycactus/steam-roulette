package ru.luckycactus.steamroulette.presentation.utils

import android.graphics.Color
import androidx.palette.graphics.Palette

object PaletteUtils {
    const val GAME_COVER_BG_TINT_ALPHA = 0.55f
    const val CONTROLS_TINT_ALPHA = 0.2f

    fun getColorForGameCover(palette: Palette?): Int {
        if (palette == null)
            return Color.TRANSPARENT
        var color = palette.getVibrantColor(Color.TRANSPARENT)
        if (color != Color.TRANSPARENT) return color
        color = palette.getLightMutedColor(Color.TRANSPARENT)
        if (color != Color.TRANSPARENT) return color
        color = palette.getDominantColor(Color.TRANSPARENT)
        return color
    }
}