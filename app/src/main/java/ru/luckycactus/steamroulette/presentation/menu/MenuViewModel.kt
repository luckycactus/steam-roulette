package ru.luckycactus.steamroulette.presentation.menu

import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.AppModule
import ru.luckycactus.steamroulette.domain.entity.Result
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesCountUseCase
import ru.luckycactus.steamroulette.domain.games.ObserveOwnedGamesSyncsUseCase
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegate
import ru.luckycactus.steamroulette.presentation.user.UserViewModelDelegatePublic
import ru.luckycactus.steamroulette.presentation.utils.combine
import ru.luckycactus.steamroulette.presentation.utils.nullableSwitchMap
import java.util.*

class MenuViewModel(
    private val userViewModelDelegate: UserViewModelDelegate
) : ViewModel(), UserViewModelDelegatePublic by userViewModelDelegate {

    val gameCount: LiveData<Int>
    val gamesLastUpdate: LiveData<String>
    val refreshProfileState: LiveData<Boolean>

    private val _refreshProfileState = MediatorLiveData<Boolean>()

    private val observeOwnedGamesCount = AppModule.observeOwnedGamesCountUseCase
    private val observeOwnedGamesSyncsUseCase = AppModule.observeOwnedGamesSyncsUseCase
    private val resourceManager = AppModule.resourceManager

    fun refreshProfile() {
        userViewModelDelegate.fetchUserAndGames()
    }

    init {
        gameCount = userViewModelDelegate.observeCurrentUserSteamId().nullableSwitchMap(0) {
            observeOwnedGamesCount(ObserveOwnedGamesCountUseCase.Params(it))
        }

        gamesLastUpdate =
            userViewModelDelegate.observeCurrentUserSteamId().nullableSwitchMap(Date(0)) {
                observeOwnedGamesSyncsUseCase(ObserveOwnedGamesSyncsUseCase.Params(it))
            }.map {
                val ago = if (it.time <= 0)
                    resourceManager.getString(R.string.never)
                else
                    DateUtils.getRelativeTimeSpanString(
                        it.time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                resourceManager.getString(R.string.last_sync, ago)
            }

        refreshProfileState = userViewModelDelegate.fetchUserSummaryState.combine(
            userViewModelDelegate.fetchGamesState
        ) { a, b -> a || (b is Result.Loading) }
    }
}