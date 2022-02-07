package ru.luckycactus.steamroulette.presentation.ui.compose.extensions

import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.HtmlCompat

@Composable
fun annotatedStringResource(@StringRes id: Int, vararg formatArgs: Any): AnnotatedString {
    val text =  stringResource(id, *formatArgs)
    val spanned = remember(text) {
        HtmlCompat.fromHtml(text, 0)
    }
    return remember(spanned) {
        buildAnnotatedString {
            append(spanned.toString())
            spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
                val start = spanned.getSpanStart(span)
                val end = spanned.getSpanEnd(span)
                when (span) {
                    is StyleSpan -> when (span.style) {
                        Typeface.BOLD ->
                            addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                        Typeface.ITALIC ->
                            addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                        Typeface.BOLD_ITALIC ->
                            addStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                ),
                                start,
                                end
                            )
                    }
                    is UnderlineSpan ->
                        addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                    is ForegroundColorSpan ->
                        addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
                }
            }
        }
    }
}