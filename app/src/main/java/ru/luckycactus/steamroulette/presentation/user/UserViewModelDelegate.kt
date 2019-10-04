package ru.luckycactus.steamroulette.presentation.user

import androidx.lifecycle.*
import ru.luckycactus.steamroulette.domain.entity.Result
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary

interface UserViewModelDelegatePublic {
    val userSummary: LiveData<UserSummary?>
}

//todo name
interface UserViewModelDelegate : UserViewModelDelegatePublic {
    val currentUserSteamId: SteamId?
    val fetchGamesState: LiveData<Result<Unit>>
    val fetchUserSummaryState: LiveData<Boolean>
    fun observeCurrentUserSteamId(): LiveData<SteamId?>
    fun fetchUserAndGames()
    fun fetchGames()
    fun clearHiddenGames()
}