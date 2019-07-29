package ru.luckycactus.steamroulette.domain.login

import ru.luckycactus.steamroulette.data.login.LoginRepository
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId

class ResolveVanityUrlUseCase(
    private val loginRepository: LoginRepository
): SuspendUseCase<String, SteamId>() {

    override suspend fun getResult(params: String): SteamId {
        return loginRepository.resolveVanityUrl(params)
    }

}