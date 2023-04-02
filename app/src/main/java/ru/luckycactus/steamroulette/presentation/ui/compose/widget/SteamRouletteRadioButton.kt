package ru.luckycactus.steamroulette.presentation.ui.compose.widget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SteamRouletteRadioButton(
    text: String,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    Row(
        modifier
            .defaultMinSize(minWidth = Dp.Infinity)
            .selectable(selected, onClick = onSelected)
            .padding(padding)
    ) {
        RadioButton(
            selected,
            onClick = onSelected,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}