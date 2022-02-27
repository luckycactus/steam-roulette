package ru.luckycactus.steamroulette.presentation.features.about

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsHeight
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.domain.about.entity.LicenseType
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme

//todo compose alpha
@Composable
fun AppLibrariesScreen(
    viewModel: AppLibrariesViewModel,
    onBackClick: () -> Unit,
) {
    val libraries by viewModel.libraries.collectAsState()
    AppLibrariesContent(libraries, onBackClick, viewModel::onLibraryClick)
}

@Composable
fun AppLibrariesContent(
    libraries: List<AppLibrary>,
    onBackClick: () -> Unit,
    onLibraryClick: (AppLibrary) -> Unit
) {
    Surface(
        color = MaterialTheme.colors.background
    ) {
        LazyColumn {
            item {
                Spacer(
                    modifier = Modifier
                        .background(MaterialTheme.colors.primarySurface)
                        .statusBarsHeight()
                        .fillMaxWidth()
                )
            }

            item {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.open_source_libraries),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    elevation = 0.dp, // todo compose
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }

            items(
                libraries.size,
                key = { libraries[it].name }
            ) {
                AppLibraryItem(
                    lib = libraries[it],
                    onItemClick = { onLibraryClick(libraries[it]) }
                )
            }
        }
    }
}

@Composable
fun AppLibraryItem(
    lib: AppLibrary,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(dimensionResource(id = R.dimen.default_activity_margin))
    ) {
        Text(
            stringResource(R.string.app_library_template, lib.author, lib.name),
            style = MaterialTheme.typography.h6
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                lib.licenseType.title,
                style = MaterialTheme.typography.subtitle1
            )
        }
    }
}

@Preview
@Composable
fun AppLibrariesContentPreview() {
    SteamRouletteTheme {
        val libraries = listOf(
            AppLibrary("Google", "Material Components", "", LicenseType.Apache2)
        )
        AppLibrariesContent(libraries, { }, { })
    }
}