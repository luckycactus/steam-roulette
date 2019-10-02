package ru.luckycactus.steamroulette.domain.user

import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.entity.SteamId

//todo naming
class FetchUserSummaryUseCase(
    private val userRepository: UserRepository
) : SuspendUseCase<FetchUserSummaryUseCase.Params, Unit>() {

    override suspend fun getResult(params: Params) {
        userRepository.refreshUserSummary(
            params.steamId,
            if (params.reload) CachePolicy.REMOTE else CachePolicy.CACHE_IF_VALID
        )
    }

    data class Params(
        val steamId: SteamId,
        val reload: Boolean
    )
}