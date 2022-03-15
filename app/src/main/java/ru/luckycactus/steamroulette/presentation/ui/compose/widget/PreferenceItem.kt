package ru.luckycactus.steamroulette.presentation.ui.compose.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme

@Composable
fun PreferenceItem(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: PreferenceItemStyle = PreferenceItemStyle.FORWARD
) {
    val alpha = if (enabled) ContentAlpha.high else ContentAlpha.disabled
    Row(
        modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = Dimens.spacingNormal, vertical = Dimens.spacingSmall)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.body2,
                    overflow = TextOverflow.Ellipsis // todo compose center
                )
            }
        }

        val icon = when (style) {
            PreferenceItemStyle.FORWARD -> Icons.Default.ChevronRight
            PreferenceItemStyle.DROPDOWN -> Icons.Default.KeyboardArrowDown
        }

        Spacer(modifier = Modifier.width(Dimens.spacingSmall))

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colors.onBackground
        )
    }
}

enum class PreferenceItemStyle {
    FORWARD,
    DROPDOWN
}

@Composable
@Preview
fun PreferenceItemPreview() {
    SteamRouletteTheme {
        PreferenceItem(
            title = "Max in-game time",
            value = "Without restrictions",
            onClick = {}
        )
    }
}