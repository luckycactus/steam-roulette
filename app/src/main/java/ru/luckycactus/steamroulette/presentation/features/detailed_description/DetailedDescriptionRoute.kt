package ru.luckycactus.steamroulette.presentation.features.detailed_description

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.SteamRouletteAppBar
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideImageGetter

// todo compose appbarlayout

@Composable
fun DetailedDescriptionRoute(
    gameTitle: String,
    description: String,
    onBackClick: () -> Unit
) {
    DetailedDescriptionScreen(gameTitle, description, onBackClick)
}

@Composable
fun DetailedDescriptionScreen(
    gameTitle: String,
    description: String,
    onBackClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colors.background
    ) {
        Column {
            SteamRouletteAppBar(
                title = gameTitle,
                subtitle = stringResource(id = R.string.game_description),
                onNavigationIconClick = onBackClick,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )

            AndroidView(
                factory = { context ->
                    TextView(context).apply {
                        text = HtmlCompat.fromHtml(
                            description,
                            0,
                            GlideImageGetter(this, true, null),
                            null
                        )
                        movementMethod = LinkMovementMethod.getInstance()
                        setTextAppearance(R.style.TextAppearance_App_Body1)
                    }
                },
                modifier = Modifier.verticalScroll(rememberScrollState())
                    .padding(Dimens.defaultActivityMargin)
            )
        }
    }
}