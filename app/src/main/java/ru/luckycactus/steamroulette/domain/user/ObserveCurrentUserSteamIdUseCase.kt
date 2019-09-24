package ru.luckycactus.steamroulette.domain.user

import androidx.lifecycle.LiveData
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId

class ObserveCurrentUserSteamIdUseCase(
    private val userRepository: UserRepository
) : UseCase<Unit?, LiveData<SteamId?>>() {

    override fun getResult(params: Unit?): LiveData<SteamId?> {
        return userRepository.observeCurrentUserSteamId()
    }
}