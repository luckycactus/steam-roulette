package ru.luckycactus.steamroulette.presentation.ui.compose.theme

import android.content.Context
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
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

    MaterialTheme(
        colors = themeParams.colors ?: MaterialTheme.colors,
        typography = themeParams.typography ?: MaterialTheme.typography,
        shapes = themeParams.shapes ?: MaterialTheme.shapes,
    ) {
        // We update the LocalContentColor to match our onBackground. This allows the default
        // content color to be more appropriate to the theme background
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colors.onBackground,
            content = content
        )
    }
}