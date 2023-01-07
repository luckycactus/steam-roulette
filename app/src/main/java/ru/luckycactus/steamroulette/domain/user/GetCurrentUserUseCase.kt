package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject

@Reusable
class GetCurrentUserUseCase @Inject constructor(
    private val userSessionRepository: UserSessionRepository
) {
    operator fun invoke(): SteamId? = userSessionRepository.currentUser
}