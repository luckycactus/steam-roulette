package ru.luckycactus.steamroulette.presentation.ui.compose.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.ui.widget.ContentState

// todo compose refactor?
@Composable
fun DataPlaceholder(
    contentState: ContentState,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        when (contentState) {
            ContentState.Loading -> ProgressBar(Modifier.size(48.dp))
            is ContentState.Placeholder -> Placeholder(contentState, onButtonClick)
            ContentState.Success -> {}
        }
    }
}

@Composable
private fun Placeholder(
    state: ContentState.Placeholder,
    onButtonClick: () -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(Dimens.defaultActivityMargin),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        getPlaceholderTitle(state.titleType)?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = state.message,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )
        getPlaceholderButton(buttonType = state.buttonType)?.let { text ->
            TextButton(onClick = onButtonClick) {
                Text(text)
            }
        }
    }
}

@Composable
private fun getPlaceholderTitle(titleType: ContentState.TitleType): String? {
    return when (titleType) {
        is ContentState.TitleType.Custom -> titleType.text
        ContentState.TitleType.DefaultEmpty -> stringResource(id = R.string.placeholder_empty_title)
        ContentState.TitleType.DefaultError -> stringResource(id = R.string.placeholder_error_title)
        ContentState.TitleType.None -> null
    }
}

@Composable
private fun getPlaceholderButton(buttonType: ContentState.ButtonType): String? {
    return when (buttonType) {
        is ContentState.ButtonType.Custom -> buttonType.text
        ContentState.ButtonType.Default -> stringResource(id = R.string.retry)
        ContentState.ButtonType.None -> null
    }
}

@Composable
@Preview
private fun ProgressBarPreview() {
    SteamRouletteTheme {
        DataPlaceholder(contentState = ContentState.Loading, {})
    }
}

@Composable
@Preview
private fun ErrorPreview() {
    SteamRouletteTheme {
        DataPlaceholder(contentState = ContentState.errorPlaceholder("Error description"), {})
    }
}
