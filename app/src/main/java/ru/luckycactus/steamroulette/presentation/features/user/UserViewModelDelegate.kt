package ru.luckycactus.steamroulette.presentation.features.user

import kotlinx.coroutines.flow.StateFlow
import ru.luckycactus.steamroulette.domain.core.RequestState

interface UserViewModelDelegate {
    val fetchGamesState: StateFlow<RequestState<Unit>>
    val fetchUserSummaryState: StateFlow<Boolean>
    fun fetchUserAndGames()
    fun fetchGames()
    fun logout()
}