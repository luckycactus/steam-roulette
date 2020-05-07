package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.UseCase
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import javax.inject.Inject

@Reusable
class ObserveUserSummaryUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<SteamId, Flow<UserSummary>>() {

    override fun getResult(params: SteamId): Flow<UserSummary> =
        userRepository.observeUserSummary(params)
}