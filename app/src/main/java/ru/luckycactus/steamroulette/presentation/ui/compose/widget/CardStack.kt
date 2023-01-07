package ru.luckycactus.steamroulette.presentation.ui.compose.widget

import androidx.compose.animation.core.*
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sign

@Composable
fun <T : Any> CardStack(
    items: List<T>,
    onSwipe: (SwipeDirection) -> Unit,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    state: CardStackState = rememberCardStackState(),
    onSwipeProgress: (Float) -> Unit = {},
    itemKey: (T) -> Any = { it },
    topCardOverlay: @Composable BoxScope.(swipeProgress: Float) -> Unit = {},
    maxChildren: Int = 3,
    scaleGap: Float = 0.2f,
    swipeThreshold: Float = 0.5f,
    itemContent: @Composable (item: T) -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    var swipeInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(items) {
        snapshotFlow {
            items.firstOrNull()
        }.onEach {
            offsetX.snapTo(0f)
            swipeInProgress = false
        }.collect()
    }

    var stackWidth by remember { mutableStateOf(0) }
    var topCardWidth by remember { mutableStateOf(0) }
    val swipeDistance by derivedStateOf {
        topCardWidth * swipeThreshold
    }
    val animationDistance by derivedStateOf {
        topCardWidth * 1.3f + (stackWidth - topCardWidth) / 2
    }
    val animationProgress by derivedStateOf {
        if (animationDistance != 0f) {
            (offsetX.value / animationDistance).coerceIn(-1f, 1f)
        } else 0f
    }

    LaunchedEffect(animationProgress) {
        onSwipeProgress(animationProgress)
    }

    var touch by remember { mutableStateOf(false) }
    val touchScale by animateFloatAsState(
        targetValue = if (touch) 1.02f else 1f,
        animationSpec = tween(durationMillis = 200)
    )

    suspend fun swipe(direction: SwipeDirection, initialVelocity: Float = 0f) {
        swipeInProgress = true
        val sign = when (direction) {
            SwipeDirection.LEFT -> -1
            SwipeDirection.RIGHT -> 1
        }
        offsetX.animateTo(
            targetValue = sign * animationDistance,
            initialVelocity = initialVelocity,
            animationSpec = tween(300)
        )
        onSwipe(direction)
    }

    LaunchedEffect(state) {
        state.swipeEvents
            .collect { direction ->
                if (!swipeInProgress) {
                    swipe(direction)
                }
            }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val topCardModifier =
        Modifier
            .pointerInput(Unit) {
                val decay = splineBasedDecay<Float>(this)
                val velocityTracker = VelocityTracker()
                coroutineScope {
                    detectHorizontalDragGestures(
                        onDragStart = {
                              velocityTracker.resetTracking()
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            velocityTracker.addPointerInputChange(change)
                            launch {
                                offsetX.snapTo(offsetX.value + dragAmount)
                            }
                            if (change.positionChange() != Offset.Zero) change.consume()
                        },
                        onDragEnd = {
                            val velocity = velocityTracker.calculateVelocity().x
                            val targetOffsetX = decay.calculateTargetValue(offsetX.value, velocity)

                            offsetX.updateBounds(-animationDistance, animationDistance)
                            launch {
                                if (targetOffsetX.absoluteValue > swipeDistance) {
                                    val direction = when {
                                        sign(targetOffsetX) < 0 -> SwipeDirection.LEFT
                                        else -> SwipeDirection.RIGHT
                                    }
                                    swipe(direction, velocity)
                                } else {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        initialVelocity = velocity,
                                        animationSpec = tween(300)
                                    )
                                }
                            }
                        }
                    )
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onItemClick(items.first())
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown()
                        touch = true
                        do {
                            val event = awaitPointerEvent()
                        } while (event.changes.any { it.pressed })
                        touch = false
                    }
                }
            }
            .onSizeChanged { topCardWidth = it.width }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .onSizeChanged { stackWidth = it.width }
    ) {
        for (i in (0 until minOf(maxChildren, items.size)).reversed()) {
            val item = items[i]
            key(itemKey(item)) {
                val modifier = remember(i) {
                    Modifier
                        .graphicsLayer {
                            translationX = if (i == 0) offsetX.value else 0f

                            val scale = when {
                                i == 0 -> touchScale
                                else -> {
                                    val easedProgress =
                                        EaseOutQuad.transform(abs(animationProgress))
                                    lerp(1f - i * scaleGap, 1f - (i - 1) * scaleGap, easedProgress)
                                }
                            }
                            scaleX = scale
                            scaleY = scale

                            rotationZ = if (i == 0) {
                                animationProgress.coerceIn(-1f, 1f) * 15
                            } else 0f

                            alpha = if (i >= 2) 0f else 1f
                        }
                        .then(if (i == 0) topCardModifier else Modifier)
                }
                Box(modifier) {
                    itemContent(item)
                    if (i == 0) {
                        val progress = if (swipeDistance != 0f) offsetX.value / swipeDistance else 0f
                        topCardOverlay(progress.coerceIn(-1f, 1f))
                    }
                }
            }
        }
    }
}

@Composable
fun rememberCardStackState() = remember { CardStackState() }

class CardStackState {
    private val _swipeEvents = MutableSharedFlow<SwipeDirection>(extraBufferCapacity = 1)
    val swipeEvents = _swipeEvents.asSharedFlow()

    fun swipe(direction: SwipeDirection) {
        _swipeEvents.tryEmit(direction)
    }
}

enum class SwipeDirection {
    LEFT, RIGHT
}