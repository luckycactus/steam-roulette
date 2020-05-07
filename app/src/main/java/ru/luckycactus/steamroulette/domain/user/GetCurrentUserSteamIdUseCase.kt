package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.UseCase
import javax.inject.Inject

@Reusable
class GetCurrentUserSteamIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<Unit?, SteamId?>() {

    override fun getResult(params: Unit?): SteamId? =
        userRepository.getCurrentUserSteamId()
}