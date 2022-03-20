package ru.luckycactus.steamroulette.presentation.ui.compose.widget

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils.headerImage
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils.libraryPortraitImage
import ru.luckycactus.steamroulette.domain.games.entity.GameUrlUtils.libraryPortraitImageHD
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import ru.luckycactus.steamroulette.presentation.utils.coil.CoverBlurTransformation
import ru.luckycactus.steamroulette.presentation.utils.coil.CoverGlareTransformation
import ru.luckycactus.steamroulette.presentation.utils.lazyNonThreadSafe

// todo compose contentdescription
@Composable
fun GameCard(
    game: GameHeader,
    modifier: Modifier = Modifier,
    imageType: GameCardImageType = GameCardImageType.HD,
    defaultTextSize: TextUnit = 20.sp,
    enableMemoryCache: Boolean = false,
    onBitmapReady: ((Bitmap) -> Unit)? = null
) {
    Box(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .aspectRatio(2 / 3f)
            .fillMaxSize()
    ) {
        // todo compose hide placeholder if cover loaded
        Image(
            painterResource(id = R.drawable.cover_placeholder),
            contentDescription = null,
            Modifier.fillMaxSize()
        )

        AutoSizeText(
            text = game.name,
            fontSizeRange = FontSizeRange(10.sp, defaultTextSize),
            maxLines = 2,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(Dimens.spacingSmall)
        )

        Image(
            painter = rememberCoverPainter(game, imageType, enableMemoryCache, onBitmapReady),
            contentDescription = null,
            Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun rememberCoverPainter(
    game: GameHeader,
    imageType: GameCardImageType,
    enableMemoryCache: Boolean,
    onBitmapReady: ((Bitmap) -> Unit)?
): AsyncImagePainter {

        val painter = when (imageType) {
            GameCardImageType.HD -> {
                rememberAsyncImagePainter(
                    model = imageRequest(libraryPortraitImageHD(game.appId), enableMemoryCache),
                    placeholder = rememberAsyncImagePainter(
                        model = imageRequest(libraryPortraitImage(game.appId), enableMemoryCache, enableNetwork = false)
                    ),
                    error = rememberAsyncImagePainter(
                        model = imageRequest(libraryPortraitImage(game.appId), enableMemoryCache, enableNetwork = false),
                        error = rememberAsyncImagePainter(
                            model = imageRequest(headerImage(game.appId), enableMemoryCache)
                        )
                    )
                )
            }
            GameCardImageType.HdIfCachedOrSd -> {
                rememberAsyncImagePainter(
                    model = imageRequest(libraryPortraitImageHD(game.appId), enableMemoryCache, enableNetwork = false),
                    error = rememberAsyncImagePainter(
                        model = imageRequest(libraryPortraitImage(game.appId), enableMemoryCache),
                        error = rememberAsyncImagePainter(
                            model = imageRequest(headerImage(game.appId), enableMemoryCache)
                        )
                    )
                )
            }
            GameCardImageType.SD -> {
                rememberAsyncImagePainter(
                    model = imageRequest(libraryPortraitImage(game.appId), enableMemoryCache),
                    error = rememberAsyncImagePainter(
                        model = imageRequest(headerImage(game.appId), enableMemoryCache)
                    )
                )
            }

        }

        val result = rememberAsyncImagePainter(
            model = imageRequest(headerImage(game.appId), enableMemoryCache, enableNetwork = false),
            error = painter,
        )

        val state = rememberCurrentAsyncImagePainterState(painter = result)
        if (state is AsyncImagePainter.State.Success) {
            LaunchedEffect(state) {
                onBitmapReady?.invoke(state.result.drawable.toBitmap())
            }
        }

        return result
}

@Composable
private fun imageRequest(
    url: String,
    enableMemoryCache: Boolean,
    enableNetwork: Boolean = true,
): ImageRequest {
    return ImageRequest.Builder(LocalContext.current)
        .data(url)
        .memoryCachePolicy(if (enableMemoryCache) CachePolicy.ENABLED else CachePolicy.DISABLED)
        .networkCachePolicy(if (enableNetwork) CachePolicy.ENABLED else CachePolicy.DISABLED)
        .transformations(headerImageTransformations)
        .crossfade(true)
        .build()
}

@Composable
private fun rememberCurrentAsyncImagePainterState(painter: AsyncImagePainter): AsyncImagePainter.State {
    var currentPainter by remember { mutableStateOf(painter) }
    val state = currentPainter.state
    if (state is AsyncImagePainter.State.Error && state.painter is AsyncImagePainter) {
        currentPainter = state.painter as AsyncImagePainter
    }
    return currentPainter.state
}

private val headerImageTransformations by lazyNonThreadSafe {
    listOf(
        CoverBlurTransformation(App.getInstance().applicationContext, 25, 100, 0.5f),
        CoverGlareTransformation(
            BitmapFactory.decodeResource(
                App.getInstance().resources,
                R.drawable.cover_glare,
                BitmapFactory.Options().apply { inScaled = false }
            ).apply {
                density = Bitmap.DENSITY_NONE
            }
        )
    )
}

enum class GameCardImageType {
    SD, HD, HdIfCachedOrSd
}