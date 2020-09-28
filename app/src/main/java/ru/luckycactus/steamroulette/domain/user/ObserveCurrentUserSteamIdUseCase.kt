package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.UseCase
import javax.inject.Inject

@Reusable
class ObserveCurrentUserSteamIdUseCase @Inject constructor(
    private val userSessionRepository: UserSessionRepository
) : UseCase<Unit?, Flow<SteamId?>>() {

    override fun execute(params: Unit?): Flow<SteamId?> {
        return userSessionRepository.observeCurrentUser()
    }
}