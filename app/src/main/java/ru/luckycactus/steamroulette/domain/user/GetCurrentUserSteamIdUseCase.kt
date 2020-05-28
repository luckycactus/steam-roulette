package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractUseCase
import javax.inject.Inject

@Reusable
class GetCurrentUserSteamIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) : AbstractUseCase<Unit?, SteamId?>() {

    override fun execute(params: Unit?): SteamId? =
        userRepository.getCurrentUserSteamId()
}