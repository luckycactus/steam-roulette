package ru.luckycactus.steamroulette.presentation.features.games

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.common.Consts
import ru.luckycactus.steamroulette.presentation.features.games.LibraryViewModel.LibraryFilter
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.noRippleClickable
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.quantityStringResource
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.ui.compose.utils.SuffixVisualTransformation
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.SimpleTextField
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.SteamRouletteRadioButton
import ru.luckycactus.steamroulette.presentation.utils.bsOffsetToAlpha

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun LibraryBottomSheet(
    filteredGamesCount: Int,
    maxHours: Int,
    selectedFilter: LibraryFilter,
    selectedFilterText: String?,
    expandProgress: Float,
    onFilterChanged: (LibraryFilter) -> Unit,
    onClearFilters: () -> Unit,
    onMaxHoursChanged: (Int) -> Unit,
    animateTo: (BottomSheetValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .background(MaterialTheme.colors.surface)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val expandedAlpha = bsOffsetToAlpha(expandProgress, 0.36f, 0.67f)
        Column(
            Modifier
                .fillMaxWidth()
                .alpha(expandedAlpha)
        ) {
            Box(
                Modifier
                    .height(dimensionResource(id = R.dimen.filter_sheet_header_height))
                    .fillMaxWidth()
            ) {
                if (expandedAlpha > 0f) {
                    this@Column.AnimatedVisibility(
                        visible = selectedFilter != LibraryFilter.All,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        TextButton(
                            onClick = onClearFilters,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 12.dp)
                        ) {
                            Text(stringResource(id = R.string.clear_filters))
                        }
                    }
                    AnimatedContent(
                        filteredGamesCount,
                        modifier = Modifier.align(Alignment.Center)
                    ) { gamesCount ->
                        Text(
                            text = quantityStringResource(
                                id = R.plurals.games_count_plurals,
                                gamesCount,
                                gamesCount
                            ),
                            style = MaterialTheme.typography.h6,
                        )
                    }
                    IconButton(
                        onClick = { animateTo(BottomSheetValue.Collapsed) },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                }
            }

            @Composable
            fun FilterRadioButton(@StringRes textId: Int, filter: LibraryFilter) {
                SteamRouletteRadioButton(
                    text = stringResource(id = textId),
                    selected = filter == selectedFilter,
                    onSelected = { onFilterChanged(filter) },
                    padding = PaddingValues(horizontal = Dimens.spacingSmall)
                )
            }

            FilterRadioButton(R.string.playtime_pref_all, LibraryFilter.All)
            FilterRadioButton(R.string.playtime_pref_not_played, LibraryFilter.NotPlayed)
            FilterRadioButton(R.string.playtime_pref_max_time, LibraryFilter.Limited)
            MaxHoursTextField(maxHours, selectedFilter == LibraryFilter.Limited, onMaxHoursChanged)
            FilterRadioButton(R.string.only_hidden, LibraryFilter.Hidden)

            Spacer(modifier = Modifier.height(Dimens.defaultActivityMargin))
        }

        if (!selectedFilterText.isNullOrEmpty()) {
            val collapsedAlpha = bsOffsetToAlpha(expandProgress, 0.36f, 0f)
            if (collapsedAlpha > 0f) {
                Box(
                    Modifier
                        .height(dimensionResource(id = R.dimen.filter_sheet_header_height))
                        .fillMaxWidth()
                        .alpha(collapsedAlpha)
                ) {
                    Chip(
                        onClick = { },
                        colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.primary),
                        modifier = Modifier
                            .padding(start = Dimens.defaultActivityMargin)
                            .align(Alignment.CenterStart)
                    ) {
                        Text(text = selectedFilterText)
                    }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .noRippleClickable(
                                enabled = expandProgress == 0f,
                                onClick = { animateTo(BottomSheetValue.Expanded) })
                    )
                    IconButton(
                        onClick = onClearFilters,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                    }
                }
            }
        }

        if (expandProgress != 0f && expandProgress != 1f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .noRippleClickable {  }
            )
        }
    }
}

@Composable
private fun MaxHoursTextField(
    maxHours: Int,
    enabled: Boolean,
    onMaxHoursChanged: (Int) -> Unit
) {
    var value by remember { mutableStateOf(maxHours.toString()) }
    var hasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(maxHours) {
        if (value.isNotEmpty() || maxHours > Consts.FILTER_MIN_HOURS) {
            value = maxHours.toString()
        }
    }

    LaunchedEffect(hasFocus) {
        if (!hasFocus && value.isEmpty()) {
            value = maxHours.toString()
        }
    }

    fun updateValue(text: String) {
        val filtered = text.filter { it.isDigit() }
        val newValue = filtered.toIntOrNull() ?: 0
        val coerced = newValue.coerceIn(Consts.FILTER_MIN_HOURS, Consts.FILTER_MAX_HOURS)
        value = if (filtered.isEmpty() || newValue == 0) "" else coerced.toString()
        onMaxHoursChanged(coerced)
    }

    val context = LocalContext.current
    val transformation = remember {
        val suffix = AnnotatedString(" " + context.getString(R.string.playtime_hours_label))
        SuffixVisualTransformation(suffix)
    }
    SimpleTextField(
        value = value,
        enabled = enabled,
        onValueChange = ::updateValue,
        visualTransformation = transformation,
        textStyle = MaterialTheme.typography.body2,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, end = Dimens.defaultActivityMargin)
            .onFocusChanged { hasFocus = it.hasFocus }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
private fun LibraryFilterBottomSheetPreview() {
    SteamRouletteTheme {
        var filter by remember { mutableStateOf(LibraryFilter.All) }
        val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Expanded)
        val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
        BottomSheetScaffold(
            sheetContent = {
                LibraryBottomSheet(
                    filteredGamesCount = 1234,
                    maxHours = 123,
                    selectedFilter = filter,
                    selectedFilterText = "< 123 hours",
                    expandProgress = 1f,
                    onFilterChanged = { filter = it },
                    onClearFilters = { },
                    onMaxHoursChanged = {},
                    animateTo = {}
                )
            },
            scaffoldState = scaffoldState,
            modifier = Modifier.height(400.dp)
        ) {
        }
    }
}
