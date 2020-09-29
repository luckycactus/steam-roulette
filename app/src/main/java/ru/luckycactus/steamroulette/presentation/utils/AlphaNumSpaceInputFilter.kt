package ru.luckycactus.steamroulette.presentation.utils

import android.text.InputFilter
import android.text.Spanned

class AlphaNumSpaceInputFilter : InputFilter {
    private val pattern = "[\\w ]*".toRegex().toPattern()

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        if (source.isNullOrBlank())
            return null
        val matcher = pattern.matcher(source).region(start, end)
        if (matcher.matches())
            return null
        return ""
    }
}