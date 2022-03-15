package ru.luckycactus.steamroulette.presentation.ui.compose.widget

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens

@Composable
fun SteamRouletteDivider() = Divider(
    color = colorResource(id = R.color.divider),
    thickness = with(LocalDensity.current) { 1.toDp() },
    modifier = Modifier.padding(vertical = Dimens.spacingSmall)
)