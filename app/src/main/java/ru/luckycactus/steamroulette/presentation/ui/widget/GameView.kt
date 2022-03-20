package ru.luckycactus.steamroulette.presentation.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.unit.sp
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.ui.compose.theme.SteamRouletteTheme
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.GameCard
import ru.luckycactus.steamroulette.presentation.ui.compose.widget.GameCardImageType

class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbstractComposeView(context, attrs) {

    var memoryCacheEnabled = false
    var onBitmapReady: ((Bitmap) -> Unit)? = null

    var game by mutableStateOf<GameHeader?>(null)
    var imageType by mutableStateOf(GameCardImageType.HD)
    var defaultTextSize by mutableStateOf(20.sp)

    @Composable
    override fun Content() {
        SteamRouletteTheme {
            game?.let {
                GameCard(
                    game = it,
                    enableMemoryCache = memoryCacheEnabled,
                    imageType = imageType,
                    defaultTextSize = defaultTextSize,
                    onBitmapReady = onBitmapReady
                )
            }
        }
    }
}