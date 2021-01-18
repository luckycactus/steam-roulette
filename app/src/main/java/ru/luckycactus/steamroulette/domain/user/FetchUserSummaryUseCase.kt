package ru.luckycactus.steamroulette.domain.user

import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.core.usecase.ResultSuspendUseCase
import javax.inject.Inject

class FetchUserSummaryUseCase @Inject constructor(
    private val userRepository: UserRepository
) : ResultSuspendUseCase<FetchUserSummaryUseCase.Params, Unit>() {

    override suspend fun getResult(params: Params) {
        val cachePolicy = if (params.reload) CachePolicy.Remote else CachePolicy.CacheOrRemote
        userRepository.fetchUserSummary(cachePolicy)
    }

    data class Params(
        val reload: Boolean
    )
}