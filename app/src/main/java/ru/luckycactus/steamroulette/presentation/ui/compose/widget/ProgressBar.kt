package ru.luckycactus.steamroulette.presentation.ui.compose.widget

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import ru.luckycactus.steamroulette.presentation.ui.widget.LuxuryProgressBar

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface
) {
    AndroidView(
        factory = {
            LuxuryProgressBar(it).apply {
                this.color = color.toArgb()
            }
        },
        modifier
    )
}