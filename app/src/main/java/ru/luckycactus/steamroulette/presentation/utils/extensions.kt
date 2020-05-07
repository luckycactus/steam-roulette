package ru.luckycactus.steamroulette.presentation.utils

import androidx.lifecycle.ViewModel
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.data.core.NetworkConnectionException
import ru.luckycactus.steamroulette.data.core.ServerException
import ru.luckycactus.steamroulette.domain.core.ResourceManager

fun ViewModel.getCommonErrorDescription(resourceManager: ResourceManager, e: Exception): String {
    return resourceManager.getString(
        when (e) {
            is ServerException -> R.string.error_steam_api_unavailable
            is NetworkConnectionException -> R.string.error_check_your_connection
            else -> R.string.error_unknown
        }
    )
}


