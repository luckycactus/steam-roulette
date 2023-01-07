package ru.luckycactus.steamroulette.presentation.features.login

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.composethemeadapter.MdcTheme
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.annotatedStringResource
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.observeWithLifecycle
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.ProgressBar

@Composable
fun LoginRoute(viewModel: LoginViewModel, onHintClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
    ) {
        val uiState by viewModel.state.collectAsState()

        val snackBarHostState = remember { SnackbarHostState() }
        viewModel.errorMessages.observeWithLifecycle {
            snackBarHostState.showSnackbar(it)
        }

        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        Crossfade(targetState = uiState.isLoading) { isLoading ->
            if (isLoading) {
                ProgressBar()
            } else {
                LoginScreenContent(
                    steamId = uiState.steamIdInput,
                    buttonEnabled = uiState.loginButtonEnabled,
                    onSteamIdChange = viewModel::onSteamIdInputChanged,
                    onLoginClick = viewModel::onSteamIdConfirmed,
                    onHintClick = onHintClick
                )
            }
        }
    }
}

@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    steamId: String,
    buttonEnabled: Boolean,
    onSteamIdChange: (String) -> Unit,
    onLoginClick: (String) -> Unit,
    onHintClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .imePadding()
            .padding(16.dp)
    ) {
        AppName(modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.weight(1f))

        SteamIdInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            steamId = steamId,
            onSteamIdChange = onSteamIdChange
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onLoginClick(steamId) },
            enabled = buttonEnabled
        ) {
            Text(text = stringResource(id = android.R.string.ok))
        }

        SteamIdHint(
            modifier = Modifier
                .height(36.dp)
                .wrapContentHeight()
                .align(Alignment.CenterHorizontally),
            onHintClick
        )

        PrivacyHint(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PrivacyHint(
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = annotatedStringResource(id = R.string.login_privacy_hint),
        style = MaterialTheme.typography.caption,
        textAlign = TextAlign.Center,
        fontSize = 13.sp
    )
}

@Composable
private fun SteamIdInput(
    steamId: String,
    onSteamIdChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        modifier = modifier,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
        value = steamId,
        maxLines = 1,
        label = { Text(text = stringResource(id = R.string.user_id_hint)) },
        onValueChange = { onSteamIdChange(it) }
    )
}

@Composable
private fun AppName(
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.h4,
        color = MaterialTheme.colors.onPrimary,
        text = stringResource(id = R.string.app_name)
    )
}

@Composable
private fun SteamIdHint(
    modifier: Modifier = Modifier,
    onHintClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val color by animateColorAsState(
        targetValue = if (pressed) {
            MaterialTheme.colors.primaryVariant
        } else {
            MaterialTheme.colors.primary
        }
    )
    Text(
        text = stringResource(id = R.string.login_button_steamid_help),
        style = MaterialTheme.typography.caption,
        fontSize = 14.sp,
        color = color,
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onHintClick() }
                )
            }
    )
}

@Preview
@Composable
fun LoginScreenContentPreview() {
    MdcTheme {
        LoginScreenContent(
            modifier = Modifier.background(MaterialTheme.colors.background),
            steamId = "luckycactus",
            buttonEnabled = true,
            onSteamIdChange = {},
            onLoginClick = {},
            onHintClick = {}
        )
    }
}