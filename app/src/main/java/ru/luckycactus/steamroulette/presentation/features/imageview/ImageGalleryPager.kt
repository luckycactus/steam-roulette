package ru.luckycactus.steamroulette.presentation.features.imageview

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.presentation.ui.compose.Dimens
import kotlin.math.PI
import kotlin.math.abs

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 3f
private const val DOUBLE_TAP_SCALE = 1f

@OptIn(ExperimentalPagerApi::class)
@Composable
fun <T> ImageGalleryPager(
    items: List<T>,
    initialPage: Int,
    imageUrl: (T) -> String,
    thumbnailUrl: ((T) -> String?)?,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        count = items.size,
        state = rememberPagerState(initialPage),
        itemSpacing = Dimens.spacingNormal,
        modifier = modifier
            .background(Color.Black)
    ) { page ->
        val item = items[page]
        ZoomableImage(
            url = imageUrl(item),
            thumbnail = thumbnailUrl?.invoke(item),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ZoomableImage(
    url: String,
    thumbnail: String?,
    modifier: Modifier = Modifier,
) {
    val zoomAnimationScope = rememberCoroutineScope()

    var contentSize by remember { mutableStateOf(IntSize.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    var scale by remember { mutableStateOf(1f) }
    var translation by remember { mutableStateOf(Offset.Zero) }

    var translationXRange by remember { mutableStateOf(0f..0f) }
    var translationYRange by remember { mutableStateOf(0f..0f) }

    fun calculateTranslationRange(
        containerSize: Int,
        contentSize: Int,
        scale: Float
    ): ClosedFloatingPointRange<Float> {
        val halfSizeDiff = (containerSize - contentSize) / 2
        val scaledContentSize = contentSize * scale
        return if (scaledContentSize > containerSize) {
            val min = (containerSize - (containerSize - halfSizeDiff) * scale) / scale
            val max = -halfSizeDiff.toFloat()
            min..max
        } else {
            val value = (containerSize - containerSize * scale) / 2f / scale
            value..value
        }
    }

    fun updateTranslationRanges(scale: Float) {
        translationXRange = calculateTranslationRange(containerSize.width, contentSize.width, scale)
        translationYRange =
            calculateTranslationRange(containerSize.height, contentSize.height, scale)
    }


    fun updatePosition(centroid: Offset, pan: Offset, newScale: Float) {
        val newScale = newScale.coerceIn(MIN_SCALE, MAX_SCALE)
        translation = translation + (centroid / newScale - centroid / scale) + pan / newScale
        updateTranslationRanges(newScale)
        if (translation.x !in translationXRange || translation.y !in translationYRange) {
            translation = translation.copy(
                x = translation.x.coerceIn(translationXRange),
                y = translation.y.coerceIn(translationYRange),
            )
        }
        scale = newScale
    }


    Box(modifier = modifier
        .pointerInput(Unit) {
            detectTransformGestures(
                consumeChanges = { scale != 1f }
            ) { centroid, pan, zoom, rotation ->
                zoomAnimationScope.coroutineContext.cancelChildren()
                updatePosition(centroid, pan, scale * zoom)
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(onDoubleTap = { point ->
                val targetScale = when {
                    scale < MAX_SCALE -> (scale + DOUBLE_TAP_SCALE).coerceAtMost(Float.MAX_VALUE)
                    else -> 1f
                }
                zoomAnimationScope.launch {
                    animate(scale, targetScale) { value, velocity ->
                        updatePosition(point, pan = Offset.Zero, newScale = value)
                    }
                }
            })
        }) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = translation.x * scale
                    translationY = translation.y * scale
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) {
            SubcomposeAsyncImage(
                modifier = Modifier
                    .align(Center)
                    .fillMaxWidth()
                    .wrapContentHeight(CenterVertically)
                    .onGloballyPositioned {
                        contentSize = it.size
                        containerSize = it.parentLayoutCoordinates!!.size
                    },
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .placeholderMemoryCacheKey(thumbnail)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
            )
        }
    }
}

// copy of function from androidx.compose.foundation.gestures but with ability
// to not consume changes for give parent a chance to handle pointer event
suspend fun PointerInputScope.detectTransformGestures(
    panZoomLock: Boolean = false,
    consumeChanges: () -> Boolean =  { true },
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit
) {
    forEachGesture {
        awaitPointerEventScope {
            var rotation = 0f
            var zoom = 1f
            var pan = Offset.Zero
            var pastTouchSlop = false
            val touchSlop = viewConfiguration.touchSlop
            var lockedToPanZoom = false

            awaitFirstDown(requireUnconsumed = false)
            do {
                val event = awaitPointerEvent()
                val canceled = event.changes.fastAny { it.isConsumed }
                if (!canceled) {
                    val zoomChange = event.calculateZoom()
                    val rotationChange = event.calculateRotation()
                    val panChange = event.calculatePan()

                    if (!pastTouchSlop) {
                        zoom *= zoomChange
                        rotation += rotationChange
                        pan += panChange

                        val centroidSize = event.calculateCentroidSize(useCurrent = false)
                        val zoomMotion = abs(1 - zoom) * centroidSize
                        val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                        val panMotion = pan.getDistance()

                        if (zoomMotion > touchSlop ||
                            rotationMotion > touchSlop ||
                            panMotion > touchSlop
                        ) {
                            pastTouchSlop = true
                            lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                        }
                    }

                    if (pastTouchSlop) {
                        val centroid = event.calculateCentroid(useCurrent = false)
                        val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                        if (effectiveRotation != 0f ||
                            zoomChange != 1f ||
                            panChange != Offset.Zero
                        ) {
                            onGesture(centroid, panChange, zoomChange, effectiveRotation)
                        }
                        val consumeChanges = consumeChanges() || event.changes.size > 1 || zoomChange != 1f
                        event.changes.fastForEach {
                            if (consumeChanges && it.positionChanged()) {
                                it.consume()
                            }
                        }
                    }
                }
            } while (!canceled && event.changes.fastAny { it.pressed })
        }
    }
}