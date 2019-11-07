package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import ru.luckycactus.steamroulette.domain.entity.CachePolicy
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.entity.SteamId
import ru.luckycactus.steamroulette.domain.entity.UserSummary
import javax.inject.Inject

@Reusable
class GetUserSummaryUseCase @Inject constructor(
    private val userRepository: UserRepository
) : SuspendUseCase<GetUserSummaryUseCase.Params, UserSummary>() {

    override suspend fun getResult(params: Params): UserSummary =
        userRepository.getUserSummary(
            params.steamId,
            if (params.reload) CachePolicy.Remote else CachePolicy.CacheIfValid
        )

    data class Params(
        val steamId: SteamId,
        val reload: Boolean
    )
}
