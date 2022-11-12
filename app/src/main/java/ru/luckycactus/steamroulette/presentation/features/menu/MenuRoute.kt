package ru.luckycactus.steamroulette.presentation.features.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.user.entity.CommunityVisibleState
import ru.luckycactus.steamroulette.domain.user.entity.PersonaState
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.quantityStringResource
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.ProgressBar
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.SteamRouletteDivider
import ru.luckycactus.steamroulette.presentation.ui.widget.compose.MenuItem

@Composable
fun MenuRoute(
    viewModel: MenuViewModel,
    onExitClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    if (state != null) {
        MenuScreen(
            state!!,
            viewModel::refreshProfile,
            viewModel::onLibraryClick,
            viewModel::onAboutClick,
            onExitClick
        )
    }
}

@Composable
private fun MenuScreen(
    state: MenuViewModel.UiState,
    onRefreshClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onAboutAppClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Surface(
        Modifier.clip(MaterialTheme.shapes.large)
    ) {
        Column(
                Modifier.padding(vertical = dimensionResource(id = R.dimen.spacing_small))
        ){
            MenuHeader(
                state.userSummary,
                state.gamesCount,
                state.gamesLastUpdate,
                state.refreshState,
                onRefreshClick
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSmall))

            SteamRouletteDivider()

            MenuItem(
                text = stringResource(id = R.string.my_steam_library),
                icon = Icons.Filled.Apps,
                withChevron = true,
                onClick = onLibraryClick,
            )

            MenuItem(
                text = stringResource(id = R.string.about_app),
                icon = Icons.Filled.Info,
                withChevron = true,
                onClick = onAboutAppClick,
            )

            MenuItem(
                text = stringResource(id = R.string.log_out),
                icon = Icons.Default.Logout,
                onClick = onExitClick,
            )
        }
    }
}

@Composable
private fun MenuHeader(
    userSummary: UserSummary,
    gamesCount: Int,
    lastUpdate: String,
    refreshState: Boolean,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .padding(
                top = dimensionResource(id = R.dimen.spacing_small),
                bottom = dimensionResource(id = R.dimen.spacing_small),
                start = dimensionResource(id = R.dimen.spacing_normal),
                end = dimensionResource(id = R.dimen.spacing_tiny)
            )
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userSummary.avatarFull)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(id = R.drawable.avatar_placeholder)
            ),
            contentDescription = null,
            Modifier.size(72.dp, 72.dp),
        )

        Column(
            Modifier
                .padding(
                    start = dimensionResource(id = R.dimen.spacing_normal),
                    end = dimensionResource(id = R.dimen.spacing_small)
                )
                .weight(1f)
        ) {
            Text(
                userSummary.personaName,
                style = MaterialTheme.typography.body1
            )

            Text(
                stringResource(
                    R.string.you_have_n_games,
                    quantityStringResource(R.plurals.games_count_plurals, gamesCount, gamesCount)
                ),
                Modifier.padding(top = dimensionResource(id = R.dimen.spacing_tiny)),
                style = MaterialTheme.typography.body2
            )

            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    lastUpdate,
                    style = MaterialTheme.typography.caption,
                    overflow = TextOverflow.Ellipsis, // todo compose center
                )
            }
        }

        if (refreshState) {
            ProgressBar(
                Modifier
                    .padding(12.dp)
                    .size(24.dp)
                    .align(CenterVertically)
            )
        } else {
            IconButton(
                onClick = { onRefreshClick() },
                Modifier.align(CenterVertically)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "refresh", // todo compose
                )
            }
        }
    }
}

@Preview
@Composable
fun MenuScreenPreview() {
    val userSummary = UserSummary(
        SteamId.fromSteam64(0),
        "luckycactus",
        CommunityVisibleState.Public,
        true,
        0,
        "",
        "",
        "",
        "",
        PersonaState.Online
    )
    val state = MenuViewModel.UiState(
        userSummary,
        1024,
        "Last games sync: yesterday",
        false
    )
    SteamRouletteTheme {
        MenuScreen(state, {}, {}, {}, {})
    }
}

