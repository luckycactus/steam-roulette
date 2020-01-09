package ru.luckycactus.steamroulette.presentation.features.user

import androidx.lifecycle.*
import ru.luckycactus.steamroulette.domain.common.Result
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary

interface UserViewModelDelegatePublic {
    val userSummary: LiveData<UserSummary>
}

interface UserViewModelDelegate : UserViewModelDelegatePublic {
    val currentUserSteamId: SteamId
    val fetchGamesState: LiveData<Result<Unit>>
    val fetchUserSummaryState: LiveData<Boolean>
    fun observeCurrentUserSteamId(): LiveData<SteamId>
    fun fetchUserAndGames()
    fun fetchGames()
}