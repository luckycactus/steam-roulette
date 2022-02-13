package ru.luckycactus.steamroulette.presentation.features.about

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.insets.statusBarsHeight
import com.google.android.material.composethemeadapter.MdcTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.adaptiveIconResource
import ru.luckycactus.steamroulette.presentation.ui.compose.extensions.toIntOffset
import ru.luckycactus.steamroulette.presentation.ui.widget.compose.MenuItem

@Composable
fun AboutScreen(
    viewModel: AboutViewModel,
    onBackClick: () -> Unit,
    onRateClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    AboutScreenContent(
        version = state.version,
        appRated = state.appRated,
        onBackClick = onBackClick,
        onSteamContactClick = { viewModel.contactDevViaSteam() },
        onSourceCodeClick = { viewModel.onSourceCodeClick() },
        onLibrariesClick = { viewModel.onUsedLibrariesClick() },
        onPrivacyPolicyClick = { viewModel.onPrivacyPolicyClick() },
        onRateClick = onRateClick
    )
}

@Composable
private fun AboutScreenContent(
    version: String,
    appRated: Boolean,
    onBackClick: () -> Unit = {},
    onSteamContactClick: () -> Unit = {},
    onSourceCodeClick: () -> Unit = {},
    onLibrariesClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onRateClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            Modifier
                .background(MaterialTheme.colors.primarySurface)
                .statusBarsHeight()
                .fillMaxWidth()
        )

        TopAppBar(
            title = { Text(text = stringResource(id = R.string.about_app)) },
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

        AboutHeader(version, onSteamContactClick)

        AboutItem(
            modifier = Modifier.padding(top = 28.dp),
            text = stringResource(id = R.string.show_app_source_code),
            icon = painterResource(id = R.drawable.ic_github),
            onClick = onSourceCodeClick
        )

        AboutItem(
            text = stringResource(id = R.string.open_source_libraries),
            icon = painterResource(id = R.drawable.ic_layers_black_24dp),
            extraIcon = painterResource(id = R.drawable.ic_chevron_right),
            onClick = onLibrariesClick
        )

        AboutItem(
            text = stringResource(id = R.string.privacy_policy),
            icon = painterResource(id = R.drawable.ic_security_24dp),
            onClick = onPrivacyPolicyClick
        )

        val rateItemBackgroundColor by animateColorAsState(
            targetValue = if (appRated) MaterialTheme.colors.surface else MaterialTheme.colors.primary
        )
        AboutItem(
            text = stringResource(id = R.string.rate_app_title),
            icon = painterResource(id = R.drawable.ic_baseline_thumb_up_24),
            backgroundColor = rateItemBackgroundColor,
            onClick = onRateClick
        )
    }
}

@Composable
private fun AboutHeader(version: String, onSteamContactClick: () -> Unit) {
    AppLogo(
        modifier = Modifier
            .padding(top = 40.dp)
            .zIndex(Float.MAX_VALUE)
    )

    Text(
        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacing_normal)),
        text = stringResource(id = R.string.app_name),
        style = MaterialTheme.typography.h5
    )

    Text(
        text = version,
        style = MaterialTheme.typography.h6
    )

    Text(
        modifier = Modifier.padding(top = 20.dp),
        text = "\u00A9 LUCKYCACTUS",
        style = MaterialTheme.typography.overline
    )

    IconButton(
        onClick = onSteamContactClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_steam),
            contentDescription = "Steam" //todo compose
        )
    }
}

@Composable
private fun AppLogo(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    var targetOffset by remember { mutableStateOf(Offset.Zero) }
    val offset by animateIntOffsetAsState(
        targetValue = targetOffset.toIntOffset(),
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )
    val clickAnimationSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessLow
        )
    }

    fun animateClick() {
        coroutineScope.launch {
            scale.animateTo(LOGO_CLICK_SCALE, clickAnimationSpec) {
                if (value >= LOGO_CLICK_SCALE) {
                    coroutineScope.launch {
                        scale.animateTo(1f, clickAnimationSpec)
                    }
                }
            }
        }
    }

    Image(
        modifier = modifier
            .absoluteOffset { offset }
            .scale(scale.value)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { animateClick() }
                )
            }
            .pointerInput(Unit) {
                coroutineScope {
                    while (true) {
                        awaitPointerEventScope {
                            awaitFirstDown()
                            targetOffset = Offset(offset.x.toFloat(), offset.y.toFloat())
                            do {
                                val event = awaitPointerEvent()
                                event.changes.forEach {
                                    targetOffset += it.positionChange()
                                }
                            } while (event.changes.any { it.pressed })
                            targetOffset = Offset.Zero
                        }
                    }
                }
            },
        bitmap = adaptiveIconResource(id = R.mipmap.ic_launcher),
        contentDescription = null
    )

    LaunchedEffect(Unit) {
        delay(100)
        animateClick()
    }
}

@Composable
fun AboutItem(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extraIcon: Painter? = null,
    backgroundColor: Color = MaterialTheme.colors.surface
) {
    MenuItem(
        modifier = modifier,
        text = text,
        icon = icon,
        extraIcon = extraIcon,
        onClick = onClick,
        textStyle = MaterialTheme.typography.body1,
        backgroundColor = backgroundColor
    )
}

@Preview
@Composable
fun AboutScreenPreview() {
    MdcTheme {
        AboutScreenContent("1.2.0 (8)", appRated = false)
    }
}

private const val LOGO_CLICK_SCALE = 1.3f