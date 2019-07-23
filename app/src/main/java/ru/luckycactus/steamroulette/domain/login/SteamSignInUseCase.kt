package ru.luckycactus.steamroulette.domain.login

import ru.luckycactus.steamroulette.data.login.LoginRepository
import ru.luckycactus.steamroulette.domain.common.UseCase
import ru.luckycactus.steamroulette.domain.user.SteamId

class SteamSignInUseCase(
    private val loginRepository: LoginRepository
): UseCase<Unit?, SteamId>() {

    override fun getResult(params: Unit?): SteamId {
        return SteamId.fromSteam64(3)
    }
}