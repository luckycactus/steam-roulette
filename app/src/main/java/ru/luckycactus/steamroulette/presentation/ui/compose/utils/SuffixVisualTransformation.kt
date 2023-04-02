package ru.luckycactus.steamroulette.presentation.ui.compose.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class SuffixVisualTransformation(
    private val suffix: AnnotatedString
): VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val offsetMapping = object: OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return offset
            }

            override fun transformedToOriginal(offset: Int): Int {
                return offset.coerceAtMost(text.length)
            }

        }
        return TransformedText(text + suffix, offsetMapping)
    }

}