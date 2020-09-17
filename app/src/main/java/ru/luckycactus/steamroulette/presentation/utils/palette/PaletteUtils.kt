package ru.luckycactus.steamroulette.presentation.utils.palette

import android.graphics.Bitmap
import android.graphics.Color
import androidx.palette.graphics.Palette

object PaletteUtils {
    const val DEFAULT_BG_TINT_ALPHA = 0.65f
    const val DEFAULT_CONTROLS_TINT_ALPHA = 0.2f

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

    fun getGameCoverPalette(bitmap: Bitmap): Palette.Builder {
        return Palette.from(bitmap)
            .maximumColorCount(24)
//            .addFilter { rgb, _ -> ColorUtils.calculateLuminance(rgb) > 0.1f }
    }
}