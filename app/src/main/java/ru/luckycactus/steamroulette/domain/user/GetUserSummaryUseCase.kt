package ru.luckycactus.steamroulette.domain.user

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.luckycactus.steamroulette.data.user.UserRepository
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary

class GetUserSummaryUseCase(
    private val userRepository: UserRepository
) : SuspendUseCase<GetUserSummaryUseCase.Params, UserSummary>() {

    override suspend fun getResult(params: Params): UserSummary =
        userRepository.getUserSummary(
            params.steamId,
            if (params.reload) CachePolicy.REMOTE else CachePolicy.CACHE_IF_VALID
        )

    @ExperimentalCoroutinesApi
    suspend fun getCacheThenRemoteIfExpired(
        coroutineScope: CoroutineScope,
        steamId: SteamId
    ) =
        userRepository.getUserSummaryCacheThenRemoteIfExpired(coroutineScope, steamId.asSteam64())

    data class Params(
        val steamId: SteamId,
        val reload: Boolean
    )
}
