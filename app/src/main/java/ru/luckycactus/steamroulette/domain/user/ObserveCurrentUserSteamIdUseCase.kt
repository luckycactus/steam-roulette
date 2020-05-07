package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.UseCase
import javax.inject.Inject

@Reusable
class ObserveCurrentUserSteamIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<Unit?, Flow<SteamId?>>() {

    override fun getResult(params: Unit?): Flow<SteamId?> {
        return userRepository.observeCurrentUserSteamId()
    }
}