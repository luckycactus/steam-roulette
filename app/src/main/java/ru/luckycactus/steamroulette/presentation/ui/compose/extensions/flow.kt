package ru.luckycactus.steamroulette.presentation.ui.compose.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

@Composable
fun <T> Flow<T>.observeWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    LaunchedEffect(lifecycleOwner) {
        launch {
            flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState).collect(FlowCollector(action))
        }
    }
}