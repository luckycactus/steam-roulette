package ru.luckycactus.steamroulette.presentation.ui.compose.theme

import android.content.Context
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import com.google.android.material.composethemeadapter.createMdcTheme

@Composable
fun SteamRouletteTheme(
    context: Context = LocalContext.current,
    content: @Composable () -> Unit
) {
    val key = context.theme

    val layoutDirection = LocalLayoutDirection.current

    val themeParams = remember(key) {
        createMdcTheme(
            context = context,
            layoutDirection = layoutDirection
        )
    }

    val typography = themeParams.typography?.let {
        it.copy(
            button = it.button.copy(
                textAlign = TextAlign.Center,
                fontFeatureSettings = "c2sc, smcp"
            )
        )
    } ?: MaterialTheme.typography

    MaterialTheme(
        colors = themeParams.colors ?: MaterialTheme.colors,
        typography = typography,
        shapes = themeParams.shapes ?: MaterialTheme.shapes,
    ) {
        // We update the LocalContentColor to match our onBackground. This allows the default
        // content color to be more appropriate to the theme background
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colors.onBackground,
            LocalContentAlpha provides 1.0f, //todo compose
            content = content
        )
    }
}