package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.SteamId
import javax.inject.Inject

@Reusable
class FetchUserSummaryUseCase @Inject constructor(
    private val userRepository: UserRepository
) : SuspendUseCase<FetchUserSummaryUseCase.Params, Unit>() {

    override suspend fun getResult(params: Params) {
        userRepository.fetchUserSummary(
            params.steamId,
            if (params.reload) CachePolicy.Remote else CachePolicy.CacheIfValid
        )
    }

    data class Params(
        val steamId: SteamId,
        val reload: Boolean
    )
}