package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import javax.inject.Inject

@Reusable
class GetCurrentUserUseCase @Inject constructor(
    private val userSessionRepository: UserSessionRepository
) : UseCase<Unit?, SteamId?>() {

    override fun execute(params: Unit?): SteamId? =
        userSessionRepository.currentUser
}