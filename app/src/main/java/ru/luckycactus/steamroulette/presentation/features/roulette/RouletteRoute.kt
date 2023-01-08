package ru.luckycactus.steamroulette.presentation.features.roulette

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.palette.graphics.Palette
import kotlinx.coroutines.flow.collectLatest
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.*
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState
import ru.luckycactus.steamroulette.presentation.utils.palette.PaletteUtils
import ru.luckycactus.steamroulette.presentation.utils.palette.TintContext
import ru.luckycactus.steamroulette.presentation.utils.palette.rememberPagerTintHelper
import ru.luckycactus.steamroulette.presentation.utils.palette.rememberTintContext
import kotlin.math.absoluteValue

@Composable
fun RouletteRoute(
    viewModel: RouletteViewModel,
    onGameClick: (GameHeader, Color?) -> Unit,
    onMenuClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    val items by viewModel.games.collectAsState(emptyList())
    val contentState by viewModel.contentState.collectAsState(ContentState.Loading)

    RouletteScreen(
        items = items ?: emptyList(),
        contentState = contentState,
        onSwipe = { direction ->
            viewModel.onGameSwiped(hide = direction == SwipeDirection.LEFT)
        },
        onGameClick = onGameClick,
        onMenuClick = onMenuClick,
        onOptionsClick = onOptionsClick,
        onRetryClick = { viewModel.onRetryClick() }
    )

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun RouletteScreen(
    items: List<GameHeader>,
    contentState: ContentState,
    onSwipe: (SwipeDirection) -> Unit,
    onGameClick: (GameHeader, Color?) -> Unit,
    onMenuClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    val tintContext = rememberTintContext()
    val pagerTintHelper = rememberPagerTintHelper<GameHeader>(tintContext)
    val stackState = rememberCardStackState()

    Scaffold(
        topBar = {
            RouletteTopBar(
                onMenuClick = onMenuClick,
                onOptionsClick = onOptionsClick,
                Modifier.statusBarsPadding()
            )
        },
        modifier = Modifier.background(tintContext.backgroundGradient()),
        backgroundColor = Color.Transparent
    ) {
        AnimatedContent(targetState = contentState) { contentState ->
            DataPlaceholder(
                contentState,
                onButtonClick = onRetryClick,
                Modifier.fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxHeight()
                        .navigationBarsPadding()
                ) {
                    CardStack(
                        state = stackState,
                        items = items,
                        onSwipe = onSwipe,
                        onSwipeProgress = {
                            pagerTintHelper.progress = it.absoluteValue
                        },
                        onItemClick = { game ->
                            val color = pagerTintHelper.getItemColor(game)
                            onGameClick(game, color)
                        },
                        itemKey = { it.appId },
                        topCardOverlay = {
                            GameCardOverlay(it, Modifier.matchParentSize())
                        },
                        modifier = Modifier
                            .aspectRatio(2 / 3f)
                            .padding(horizontal = Dimens.spacingNormal)
                    ) { game ->
                        GameCard(
                            game,
                            onBitmapReady = { bitmap ->
                                Palette.Builder(bitmap).generate { palette ->
                                    pagerTintHelper.setItemColor(
                                        game,
                                        Color(PaletteUtils.getColorForGameCover(palette))
                                    )
                                }
                            }
                        )
                    }

                    FabsRow(
                        tintContext,
                        onHideClick = { stackState.swipe(SwipeDirection.LEFT) },
                        onNextClick = { stackState.swipe(SwipeDirection.RIGHT) },
                        onGameInfoClick = {
                            val game = items.firstOrNull()
                            if (game != null) { // todo compose dry
                                val color = pagerTintHelper.getItemColor(game)
                                onGameClick(game, color)
                            }
                        },
                        modifier = Modifier
                            .padding(Dimens.spacingNormal)
                            .align(CenterHorizontally)
                    )
                }
            }
        }
    }

    LaunchedEffect(items) {
        snapshotFlow { items.firstOrNull() }
            .collectLatest {
                pagerTintHelper.setItems(items.take(2))
                pagerTintHelper.progress = 0f
            }
    }
}

@Composable
private fun RouletteTopBar(
    onMenuClick: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        backgroundColor = Color.Transparent,
        title = {},
        elevation = 0.dp,
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onOptionsClick) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
            }
        }
    )
}

@Composable
private fun GameCardOverlay(
    swipeProgress: Float,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(modifier) {
        val (hide) = createRefs()

        Icon(
            painter = painterResource(R.drawable.ic_visibility_off_24dp),
            contentDescription = null,
            tint = colorResource(id = R.color.color_overlay_hide),
            modifier = Modifier
                .constrainAs(hide) {
                    width = Dimension.percent(0.25f)
                    height = Dimension.wrapContent
                    linkTo(
                        parent.start,
                        parent.top,
                        parent.end,
                        parent.bottom,
                        startMargin = Dimens.spacingNormal,
                        topMargin = Dimens.spacingNormal,
                        endMargin = Dimens.spacingNormal,
                        bottomMargin = Dimens.spacingNormal,
                        horizontalBias = 0.9f,
                        verticalBias = 0.05f
                    )
                }
                .aspectRatio(1f)
                .rotate(20f)
                .alpha((-swipeProgress).coerceIn(0f, 1f))
        )
    }
}

@Composable
private fun FabsRow(
    tintContext: TintContext,
    onHideClick: () -> Unit,
    onNextClick: () -> Unit,
    onGameInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = tintContext.fabBackground()

    @Composable
    fun Fab(
        drawableResId: Int,
        onClick: () -> Unit
    ) {
        FloatingActionButton(
            onClick = onClick,
            backgroundColor = backgroundColor
        ) {
            Icon(
                painter = painterResource(id = drawableResId),
                contentDescription = null
            )
        }
    }

    CompositionLocalProvider(LocalElevationOverlay provides null) {
        Row(modifier, horizontalArrangement = Arrangement.Center) {
            Fab(
                drawableResId = R.drawable.ic_visibility_off_24dp,
                onClick = onHideClick
            )

            Spacer(modifier = Modifier.width(Dimens.spacingNormal))

            Fab(
                drawableResId = R.drawable.ic_info_24dp,
                onClick = onGameInfoClick
            )

            Spacer(modifier = Modifier.width(Dimens.spacingNormal))

            Fab(
                drawableResId = R.drawable.ic_chevron_right,
                onClick = onNextClick,
            )
        }
    }
}