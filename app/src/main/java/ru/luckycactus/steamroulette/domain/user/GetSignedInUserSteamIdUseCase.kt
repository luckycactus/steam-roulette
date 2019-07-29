package ru.luckycactus.steamroulette.domain.user

import ru.luckycactus.steamroulette.data.user.UserRepository
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId

class GetSignedInUserSteamIdUseCase(
    private val userRepository: UserRepository
) : UseCase<Unit?, SteamId?>() {

    override fun getResult(params: Unit?): SteamId? =
        userRepository.getSignedInUserSteamId()
}