package ru.luckycactus.steamroulette.presentation.features.system_reqs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import ru.luckycactus.steamroulette.presentation.ui.compose.PreviewData
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.fromHtml
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.SteamRouletteAppBar

// todo compose appbarlayout
// todo compose tab style

@Composable
fun SystemReqsRoute(
    gameTitle: String,
    systemReqs: List<SystemRequirements>,
    onBackClick: () -> Unit,
) {
    SystemReqsScreen(gameTitle, systemReqs, onBackClick)
}

@Composable
@OptIn(ExperimentalPagerApi::class)
private fun SystemReqsScreen(
    gameTitle: String,
    systemReqs: List<SystemRequirements>,
    onBackClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    Surface(
        color = MaterialTheme.colors.background
    ) {
        Column {
            SteamRouletteAppBar(
                title = gameTitle,
                subtitle = stringResource(id = R.string.system_requirements),
                onNavigationIconClick = onBackClick,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )

            val pagerState = rememberPagerState()

            TabRow(selectedTabIndex = pagerState.currentPage) {
                systemReqs.forEachIndexed { index, value ->
                    Tab(
                        text = { Text(value.platform.name) },
                        selected = index == pagerState.currentPage,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } }
                    )
                }
            }

            HorizontalPager(
                count = systemReqs.size,
                state = pagerState
            ) { page ->
                SystemReqsPage(systemReqs[page], Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun SystemReqsPage(
    systemReqs: SystemRequirements,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.defaultActivityMargin)
    ) {
        if (systemReqs.minimum != null) {
            Text(text = fromHtml(systemReqs.minimum))
        }
        if (systemReqs.recommended != null) {
            Text(text = fromHtml(systemReqs.recommended))
        }
    }
}

@Preview
@Composable
fun SystemReqsScreenPreview() {
    SteamRouletteTheme {
        SystemReqsScreen(
            gameTitle = "Anomaly 2",
            systemReqs = PreviewData.systemReqs
        )
    }
}