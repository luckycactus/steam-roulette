package ru.luckycactus.steamroulette.presentation.features.roulette_options

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.PreferenceItem
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.PreferenceItemStyle
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.SteamRouletteDivider

@Composable
fun RouletteOptionsScreen(
    viewModel: RouletteOptionsViewModel,
    onPlaytimeClick: () -> Unit
) {
    val playtime by viewModel.playTimePrefValue.observeAsState("")
    val hiddenGames by viewModel.hiddenGamesCount.observeAsState(0)

    RouletteOptionsScreenContent(
        playtime,
        hiddenGames,
        onPlaytimeClick,
        viewModel::onHiddenGamesClick
    )
}

@Composable
fun RouletteOptionsScreenContent(
    playtime: String,
    hiddenGames: Int,
    onPlaytimeClick: () -> Unit,
    onHiddenGamesClick: () -> Unit
) {
    Surface(
        Modifier.clip(MaterialTheme.shapes.large)
    ) {
        Column(
            Modifier.padding(vertical = Dimens.spacingSmall)
        ) {
            PreferenceItem(
                title = stringResource(id = R.string.playtime),
                value = playtime,
                onClick = { onPlaytimeClick() },
                style = PreferenceItemStyle.DROPDOWN
            )

            SteamRouletteDivider()

            PreferenceItem(
                title = stringResource(id = R.string.hidden_games_count),
                value = hiddenGames.toString(),
                onClick = { onHiddenGamesClick() },
                enabled = hiddenGames > 0
            )
        }
    }
}

@Composable
@Preview
fun RouletteOptionsScreenPreview() {
    SteamRouletteTheme {
        RouletteOptionsScreenContent("All games", 0, {}, {})
    }
}