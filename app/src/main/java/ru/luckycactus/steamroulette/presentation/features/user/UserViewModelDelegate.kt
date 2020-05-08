package ru.luckycactus.steamroulette.presentation.features.user

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.Result
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary

//todo document
interface UserViewModelDelegatePublic {
    val userSummary: LiveData<UserSummary>
}

interface UserViewModelDelegate : UserViewModelDelegatePublic {
    val isUserLoggedIn: Boolean
    val currentUserSteamId: Flow<SteamId>
    val fetchGamesState: LiveData<Result<Unit>>
    val fetchUserSummaryState: LiveData<Boolean>
    fun getCurrentUserSteamId(): SteamId
    fun fetchUserAndGames()
    fun fetchGames()
    fun resetHiddenGames()
    fun exit()
}