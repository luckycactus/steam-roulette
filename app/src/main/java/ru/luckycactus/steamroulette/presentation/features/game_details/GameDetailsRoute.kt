package ru.luckycactus.steamroulette.presentation.features.game_details

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.Screenshot
import ru.luckycactus.steamroulette.presentation.features.game_details.model.GameDetailsUiModel
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import ru.luckycactus.steamroulette.presentation.ui.compose.PreviewData
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.fromHtml
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.toIntOffset
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.DataPlaceholder
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.GameCard
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.GameCardImageType
import ru.luckycactus.steamroulette.presentation.utils.palette.PaletteUtils
import ru.luckycactus.steamroulette.presentation.utils.palette.TintContext
import ru.luckycactus.steamroulette.presentation.utils.palette.rememberTintContext

@Composable
fun GameDetailsRoute(
    viewModel: GameDetailsViewModel,
    tintColor: Color,
    onBackPressed: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    GameDetailsScreen(
        requestedId = viewModel.requestedId,
        tintColor = tintColor,
        state = state,
        onBackPressed = onBackPressed,
        onStoreClick = viewModel::onStoreClick,
        onHubClick = viewModel::onHubClick,
        onScreenshotClick = viewModel::onScreenshotClick,
        onDetailedDescriptionClick = viewModel::onDetailedDescriptionClick,
        onMetacriticClick = viewModel::onMetacriticClick,
        onSystemRequirementsClick = viewModel::onSystemRequirementsClick,
        onPlaceholderButtonClick = viewModel::onRetryClick
    )
}

@Composable
private fun GameDetailsScreen(
    requestedId: Int,
    tintColor: Color,
    state: GameDetailsViewModel.UiState,
    onBackPressed: () -> Unit = {},
    onStoreClick: () -> Unit = {},
    onHubClick: () -> Unit = {},
    onScreenshotClick: (screenshot: Screenshot) -> Unit = {},
    onDetailedDescriptionClick: () -> Unit = {},
    onMetacriticClick: () -> Unit = {},
    onSystemRequirementsClick: () -> Unit = {},
    onPlaceholderButtonClick: () -> Unit = {},
) {
    val tintContext = rememberTintContext(tintColor)
    var fabHeight by remember { mutableStateOf(0) }
    val fabHeightDp = with(LocalDensity.current) { fabHeight.toDp() }


    Scaffold(
        floatingActionButton = {
            GameDetailsFab(
                onBackPressed,
                tintContext,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .onGloballyPositioned { fabHeight = it.size.height }
            )
        }
    ) {
        GameDetailsContent(
            requestedId,
            tintContext,
            state,
            fabHeightDp,
            onStoreClick,
            onHubClick,
            onScreenshotClick,
            onDetailedDescriptionClick,
            onMetacriticClick,
            onSystemRequirementsClick,
            onPlaceholderButtonClick
        )
    }
}

@Composable
private fun GameDetailsFab(
    onBackPressed: () -> Unit,
    tintContext: TintContext,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onBackPressed,
        backgroundColor = tintContext.fabBackground(),
        modifier = modifier
    ) {
        Icon(painter = painterResource(id = R.drawable.ic_reply_24dp), contentDescription = null)
    }
}

@Composable
private fun GameDetailsContent(
    requestedId: Int,
    tintContext: TintContext,
    state: GameDetailsViewModel.UiState,
    fabHeight: Dp,
    onStoreClick: () -> Unit,
    onHubClick: () -> Unit,
    onScreenshotClick: (screenshot: Screenshot) -> Unit,
    onDetailedDescriptionClick: () -> Unit,
    onMetacriticClick: () -> Unit,
    onSystemRequirementsClick: () -> Unit,
    onPlaceholderButtonClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
    ) {

        val lazyListState = rememberLazyListState()
        var scroll by remember { mutableStateOf(Offset(0f, 0f)) }
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    scroll = scroll.copy(y = (scroll.y + consumed.y).coerceAtMost(0f))
                    return super.onPostScroll(consumed, available, source)
                }
            }
        }
        Box(
            modifier = Modifier
                .offset { scroll.toIntOffset() }
                .background(tintContext.backgroundGradient())
                .fillMaxSize()
        )

        val gameDetails = state.gameDetails
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingNormal),
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            item {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            }
            item {
                GameDetailsHeader(gameDetails) { bitmap ->
                    if (tintContext.tintColor == Color.Transparent || requestedId != gameDetails.header.appId) {
                        Palette.Builder(bitmap).generate { palette ->
                            tintContext.updateColor(Color(PaletteUtils.getColorForGameCover(palette)))
                        }
                    }
                }
            }
            item {
                GameDetailsLinks(onStoreClick, onHubClick)
            }
            if (gameDetails.screenshots.isNotEmpty()) {
                item {
                    GameDetailsScreenshots(gameDetails.screenshots, onScreenshotClick)
                }
            }
            if (gameDetails.shortDescription?.isEmpty() == false) {
                item {
                    GameDetailsShortDescription(
                        gameDetails.shortDescription,
                        onDetailedDescriptionClick,
                        onMetacriticClick,
                    )
                }
            }
            if (gameDetails.platforms != null) {
                item {
                    GameDetailsPlatforms(
                        gameDetails.platforms,
                        onSystemRequirementsClick
                    )
                }
            }
            if (!gameDetails.languages.isNullOrEmpty()) {
                item {
                    GameDetailsLanguages(gameDetails.languages)
                }
            }
            if (state.placeholder != null) {
                item {
                    DataPlaceholder(
                        state.placeholder,
                        onPlaceholderButtonClick,
                        Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(fabHeight))
            }
            item {
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
private fun GameDetailsHeader(
    gameDetails: GameDetailsUiModel,
    modifier: Modifier = Modifier,
    onImageReady: (Bitmap) -> Unit
) {
    Row(
        modifier.padding(horizontal = Dimens.defaultActivityMargin)
    ) {
        GameCard(
            game = gameDetails.header,
            Modifier.weight(1f),
            imageType = GameCardImageType.HdIfCachedOrSd,
            defaultTextSize = 16.sp,
            enableMemoryCache = true,
            onBitmapReady = onImageReady
        )

        Spacer(Modifier.width(Dimens.spacingNormal))

        Crossfade(
            targetState = gameDetails,
            Modifier.weight(2f)
        ) { gameDetails ->
            Column {
                Text(
                    text = gameDetails.header.name,
                    style = MaterialTheme.typography.h5
                )

                if (gameDetails.releaseDate != null) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = gameDetails.releaseDate,
                            style = MaterialTheme.typography.subtitle1,
                            fontSize = 14.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (gameDetails.developer != null) {
                    Text(
                        text = gameDetails.developer,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (gameDetails.publisher != null) {
                    Text(
                        text = gameDetails.publisher,
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            }
        }
    }
}

@Composable
private fun GameDetailsLinks(
    onStoreClick: () -> Unit,
    onHubClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .padding(horizontal = Dimens.defaultActivityMargin)
            .height(IntrinsicSize.Min)
    ) {
        LinkButton(
            stringResource(id = R.string.button_steam_store),
            Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = onStoreClick
        )

        Spacer(modifier = Modifier.width(Dimens.defaultActivityMargin))

        LinkButton(
            stringResource(id = R.string.button_community_hub),
            Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = onHubClick
        )
    }
}

@Composable
private fun LinkButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier,
        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.color_game_details_button)),
        elevation = null
    ) {
        Text(
            text = text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.button,
        )
    }
}

@Composable
private fun GameDetailsScreenshots(
    screenshots: List<Screenshot>,
    onScreenshotClick: (screenshot: Screenshot) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingNormal),
        contentPadding = PaddingValues(horizontal = Dimens.defaultActivityMargin),
        modifier = Modifier.height(100.dp)
    ) {
        items(
            screenshots,
            key = { it.id }
        ) { screenshot ->

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(screenshot.thumbnail)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.FillHeight,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable { onScreenshotClick(screenshot) },
                loading = {
                    ScreenshotPlaceholder()
                }
            )
        }
    }
}

@Composable
private fun ScreenshotPlaceholder() {
    Box(
        Modifier
            .fillMaxHeight()
            .aspectRatio(600f / 338f)
            .background(colorResource(id = R.color.color_screenshot_placeholder))
    )
}

@Composable
private fun GameDetailsShortDescription(
    description: GameDetailsUiModel.ShortDescription,
    onDetailedDescriptionClick: () -> Unit,
    onMetacriticClick: () -> Unit
) {
    Section(
        title = stringResource(id = R.string.game_details_description),
        onClick = onDetailedDescriptionClick.takeIf { description.detailedDescriptionAvailable }
    ) {
        Column(Modifier.fillMaxWidth()) {

            Text(
                text = fromHtml(description.value ?: ""),
                Modifier.padding(horizontal = Dimens.defaultActivityMargin)
            ) //todo compose html

            Spacer(modifier = Modifier.height(Dimens.spacingNormal))

            if (!description.genres.isNullOrEmpty()) {
                TagsLazyRow(tags = description.genres)
            }

            Spacer(modifier = Modifier.height(Dimens.spacingNormal))

            if (!description.categories.isNullOrEmpty()) {
                TagsLazyRow(tags = description.categories)
            }

            val ageResource = description.requiredAge?.let { getAgeDrawableResource(it) }
            if (description.metacriticInfo != null || ageResource != null) {
                Spacer(modifier = Modifier.height(Dimens.spacingNormal))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .padding(horizontal = Dimens.defaultActivityMargin)
                ) {
                    ageResource?.let {
                        Image(
                            painterResource(id = ageResource),
                            contentDescription = null,
                            Modifier.height(48.dp),
                            contentScale = ContentScale.FillHeight
                        )
                        Spacer(modifier = Modifier.width(Dimens.defaultActivityMargin))
                    }

                    description.metacriticInfo?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = onMetacriticClick)
                                .fillMaxHeight()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.metacritic_logo_no_text),
                                contentDescription = null,
                                Modifier
                                    .size(36.dp)
                                    .align(Alignment.CenterVertically),
                            )

                            Spacer(modifier = Modifier.width(Dimens.spacingSmall))

                            Text(
                                text = description.metacriticInfo.score.toString(),
                                color = Color.White,
                                fontSize = 30.sp,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(description.metacriticInfo.color))
                                    .padding(Dimens.spacingSmall)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getAgeDrawableResource(age: Int): Int? {
    return when (age) {
        0 -> R.drawable.age_0
        6 -> R.drawable.age_6
        12 -> R.drawable.age_12
        16 -> R.drawable.age_16
        18 -> R.drawable.age_18
        else -> null
    }
}

@Composable
private fun TagsLazyRow(
    tags: List<String>
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.spacingNormal),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
    ) {
        items(tags, key = { it }) { name ->
            TagItem(name)
        }
    }
}

@Composable
private fun TagItem(
    name: String
) {
    Text(
        name,
        style = MaterialTheme.typography.body2,
        modifier = Modifier
            .background(
                colorResource(id = R.color.color_chip_game_description),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
private fun GameDetailsPlatforms(
    platforms: GameDetailsUiModel.Platforms,
    onSystemRequirementsClick: () -> Unit
) {
    Section(
        title = stringResource(id = R.string.game_details_platforms),
        onClick = if (platforms.systemRequirementsAvailable) onSystemRequirementsClick else null
    ) {
        Row(
            Modifier.padding(horizontal = Dimens.defaultActivityMargin),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
        ) {
            if (platforms.platforms.windows) {
                PlatformIcon(painterResource(id = R.drawable.ic_windows))
            }
            if (platforms.platforms.mac) {
                PlatformIcon(painterResource(id = R.drawable.ic_apple))
            }
            if (platforms.platforms.linux) {
                PlatformIcon(painterResource(id = R.drawable.ic_steam))
            }
        }
    }
}

@Composable
private fun PlatformIcon(
    painter: Painter
) {
    Icon(
        painter,
        contentDescription = null,
        Modifier.size(28.dp),
        tint = colorResource(id = R.color.platforms_icon_tint)
    )
}

@Composable
private fun GameDetailsLanguages(
    languages: String
) {
    Section(title = stringResource(id = R.string.game_details_languages)) {
        Text(
            text = fromHtml(languages),
            Modifier.padding(horizontal = Dimens.defaultActivityMargin)
        )
    }
}

@Composable
private fun Section(
    title: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            modifier
                .let {
                    if (onClick != null) {
                        it.clickable(onClick = onClick)
                    } else it
                }
                .padding(
                    horizontal = Dimens.defaultActivityMargin,
                    vertical = Dimens.spacingSmall
                )
        ) {
            Text(
                text = title,
                Modifier.weight(1f),
                style = MaterialTheme.typography.h5,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.width(Dimens.spacingNormal))

            if (onClick != null) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacingSmall))

        content()
    }
}

@Composable
@Preview
private fun GameDetailsPreview() {
    SteamRouletteTheme {
        GameDetailsScreen(
            0,
            Color.Red,
            PreviewData.gameDetailsState
        )
    }
}

@Composable
@Preview
private fun GameDetailsLoadingPreview() {
    SteamRouletteTheme {
        GameDetailsScreen(
            0,
            Color.Red,
            PreviewData.gameDetailsLoadingState
        )
    }
}