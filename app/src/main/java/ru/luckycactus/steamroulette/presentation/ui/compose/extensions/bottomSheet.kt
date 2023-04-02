package ru.luckycactus.steamroulette.presentation.ui.compose.extensions

import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetState.rememberExpandProgressState(
): State<Float> {
    val expandProgress = remember(this) { mutableStateOf(getExpandProgress()) }
    LaunchedEffect(progress) {
        expandProgress.value = getExpandProgress()
    }
    return expandProgress
}

@OptIn(ExperimentalMaterialApi::class)
private fun BottomSheetState.getExpandProgress(): Float {
    return when (progress.to) {
        BottomSheetValue.Collapsed -> 1f - progress.fraction
        BottomSheetValue.Expanded -> progress.fraction
    }
}