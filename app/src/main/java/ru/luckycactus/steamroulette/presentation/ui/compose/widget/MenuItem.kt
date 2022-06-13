package ru.luckycactus.steamroulette.presentation.ui.widget.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import ru.luckycactus.steamroulette.R

@Composable
fun MenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    withChevron: Boolean = false,
    backgroundColor: Color = MaterialTheme.colors.surface,
    textStyle: TextStyle = MaterialTheme.typography.body2
) {
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .heightIn(min = 48.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.default_activity_margin)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null)

            Text(
                text,
                modifier = Modifier
                    .padding(start = dimensionResource(id = R.dimen.default_activity_margin))
                    .weight(1f),
                style = textStyle
            )

            if (withChevron) {
                Icon(
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.default_activity_margin)),
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null
                )
            }
        }
    }
}