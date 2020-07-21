package ru.luckycactus.steamroulette.presentation.features.user

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.RequestState

interface UserViewModelDelegate {
    val fetchGamesState: LiveData<RequestState<Unit>>
    val fetchUserSummaryState: LiveData<Boolean>
    fun fetchUserAndGames()
    fun fetchGames()
    fun logout()
}