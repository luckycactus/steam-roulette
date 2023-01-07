package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject

@Reusable
class ObserveCurrentUserSteamIdUseCase @Inject constructor(
    private val userSessionRepository: UserSessionRepository
) {

    operator fun invoke(): Flow<SteamId?> {
        return userSessionRepository.observeCurrentUser()
    }
}