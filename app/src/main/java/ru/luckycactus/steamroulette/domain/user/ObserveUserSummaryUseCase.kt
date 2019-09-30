package ru.luckycactus.steamroulette.domain.user

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary

class ObserveUserSummaryUseCase(
    private val userRepository: UserRepository
) : UseCase<SteamId, LiveData<UserSummary>>() {

    override fun getResult(params: SteamId): LiveData<UserSummary> =
        userRepository.observeUserSummary(params)
}