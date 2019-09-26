package ru.luckycactus.steamroulette.presentation.user

import androidx.lifecycle.*
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary

interface UserViewModelDelegatePublic {
    val userSummary: LiveData<UserSummary?>
    val refreshUserSummaryState: LiveData<Boolean>
}

interface UserViewModelDelegate : UserViewModelDelegatePublic {
    val currentUserSteamId: SteamId?
    fun observeCurrentUserSteamId(): LiveData<SteamId?>
    //todo into public?
    fun refreshUserSummary()
}