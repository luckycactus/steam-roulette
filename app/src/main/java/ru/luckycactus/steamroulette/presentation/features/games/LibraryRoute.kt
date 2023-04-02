package ru.luckycactus.steamroulette.presentation.features.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.LibraryGame
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import ru.luckycactus.steamroulette.presentation.ui.compose.PreviewData
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.items
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.noRippleClickable
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.rememberExpandProgressState
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.GameCard
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.GameCardImageType

@Composable
fun LibraryRoute(
    viewModel: LibraryViewModel,
    onBackClick: () -> Unit
) {
    val columns by viewModel.spanCount.observeAsState(0)
    val hasAnyFilters by viewModel.hasAnyFilters.observeAsState(false)
    val games = viewModel.games.collectAsLazyPagingItems()

    val gamesCount by viewModel.filteredGamesCount.observeAsState(0)
    val maxHours by viewModel.maxHours.observeAsState(0)
    val filter by viewModel.libraryFilter.observeAsState(LibraryViewModel.LibraryFilter.All)
    val selectedFilterText by viewModel.selectedFilterText.observeAsState("")

    LibraryScreen(
        hasAnyFilters = hasAnyFilters,
        viewModel = viewModel,
        filteredGamesCount = gamesCount,
        maxHours = maxHours,
        currentFilter = filter,
        selectedFilterText = selectedFilterText,
        columns = columns,
        games = games,
        onBackClick = onBackClick
    )
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun LibraryScreen(
    hasAnyFilters: Boolean,
    viewModel: LibraryViewModel,
    filteredGamesCount: Int,
    maxHours: Int,
    currentFilter: LibraryViewModel.LibraryFilter,
    selectedFilterText: String?,
    columns: Int,
    games: LazyPagingItems<LibraryGame>,
    onBackClick: () -> Unit
) {
    val bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)
    val coroutineScope = rememberCoroutineScope()

    val bottomSheetPeekHeight = if (hasAnyFilters) {
        with(LocalDensity.current) {
            WindowInsets.navigationBars.getBottom(this).toDp()
        } + dimensionResource(id = R.dimen.filter_sheet_header_height)
    } else 0.dp

    val expandProgress by bottomSheetState.rememberExpandProgressState()

    LaunchedEffect(Unit) {
        var prevProgress = expandProgress
        snapshotFlow { expandProgress }
            .collectLatest { newProgress ->
                if (prevProgress == 1f && newProgress < 1f) {
                    viewModel.onFilterSheetClosingStarted()
                }
                prevProgress = newProgress
            }
    }

    fun animateBottomSheetTo(value: BottomSheetValue) {
        coroutineScope.launch {
            bottomSheetState.animateTo(value)
        }
    }

    fun clearFilters() {
        viewModel.clearFilters()
        animateBottomSheetTo(BottomSheetValue.Collapsed)
    }

    val focusManager = LocalFocusManager.current
    var bottomSheetHasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(bottomSheetState.progress.to) {
        if (bottomSheetHasFocus && bottomSheetState.progress.to == BottomSheetValue.Collapsed) {
            focusManager.clearFocus()
        }
    }

    BottomSheetScaffold(
        sheetContent = {
            LibraryBottomSheet(
                filteredGamesCount = filteredGamesCount,
                maxHours = maxHours,
                selectedFilter = currentFilter,
                selectedFilterText = selectedFilterText,
                expandProgress = expandProgress,
                onFilterChanged = viewModel::onFilterSelectionChanged,
                onClearFilters = ::clearFilters,
                onMaxHoursChanged = viewModel::onMaxHoursChanged,
                animateTo = ::animateBottomSheetTo,
                modifier = Modifier
                    .onFocusChanged { bottomSheetHasFocus = it.hasFocus }
            )
        },
        scaffoldState = scaffoldState,
        sheetPeekHeight = bottomSheetPeekHeight,
    ) {
        Box {
            LibraryScreenContent(
                columns = columns,
                games = games,
                fabVisible = !hasAnyFilters,
                onBackClick = onBackClick,
                onSearchClick = { viewModel.onSearchStateChanged(true) },
                onChangeScaleClick = viewModel::onChangeScaleClick,
                onOpenFiltersClick = remember {{ animateBottomSheetTo(BottomSheetValue.Expanded) }}
            )

            // Scrim
            if (expandProgress > 0f) {
                Box(
                    Modifier
                        .matchParentSize()
                        .alpha(expandProgress)
                        .background(Color.Black.copy(alpha = 0.5f)) // todo compose
                        .noRippleClickable { animateBottomSheetTo(BottomSheetValue.Collapsed) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryScreenContent(
    columns: Int,
    games: LazyPagingItems<LibraryGame>,
    fabVisible: Boolean,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onChangeScaleClick: () -> Unit,
    onOpenFiltersClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            val scale by animateFloatAsState(targetValue = if (fabVisible) 1f else 0f)
            FloatingActionButton(
                onClick = onOpenFiltersClick,
                backgroundColor = MaterialTheme.colors.primary,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .scale(scale)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null)
            }
        },
        topBar = {
            LibraryTopAppBar(
                onBackClick,
                onSearchClick,
                onChangeScaleClick,
                Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(Dimens.defaultActivityMargin),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(paddingValues)
        ) {
            items(games, key = { it.header.appId }) { game ->
                LibraryItem(game = game, isSelected = false, Modifier.animateItemPlacement())
            }
        }
    }
}

//todo compose
@Composable
private fun LibraryItem(
    game: LibraryGame?,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    if (game != null) {
        GameCard(
            game = game.header,
            imageType = GameCardImageType.SD,
            defaultTextSize = 16.sp,
            modifier = modifier
        )
    }
}

@Composable
private fun LibraryTopAppBar(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onChangeScaleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        elevation = 0.dp, // todo compose
        title = { Text(text = stringResource(id = R.string.my_steam_library)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
//            IconButton(onClick = onSearchClick) {
//                Icon(imageVector = Icons.Default.Search, contentDescription = null)
//            }
            IconButton(onClick = onChangeScaleClick) {
                Icon(imageVector = Icons.Default.Dashboard, contentDescription = null)
            }
        }
    )
}


@Preview
@Composable
private fun LibraryPreview() {
    val games = PreviewData.games.map { LibraryGame(it, false) }
    val pagingItems = flowOf(PagingData.from(games)).collectAsLazyPagingItems()
    SteamRouletteTheme {
        LibraryScreenContent(3, pagingItems, true, {}, {}, {}, {})
    }
}