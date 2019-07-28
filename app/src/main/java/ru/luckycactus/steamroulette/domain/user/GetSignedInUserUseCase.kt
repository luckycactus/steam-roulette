package ru.luckycactus.steamroulette.domain.user

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.luckycactus.steamroulette.data.user.UserRepository
import ru.luckycactus.steamroulette.domain.CachePolicy
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase

class GetSignedInUserUseCase(
    private val userRepository: UserRepository
) : SuspendUseCase<GetSignedInUserUseCase.Params, UserSummary>() {

    override suspend fun getResult(params: Params): UserSummary {
        return userRepository.getSignedInUserSummary(if (params.reload) CachePolicy.REMOTE else CachePolicy.CACHE_IF_VALID)
    }

    @ExperimentalCoroutinesApi
    fun getCacheThenRemote(coroutineScope: CoroutineScope) = coroutineScope.getCacheThenRemote {
        userRepository.getSignedInUserSummary(it)
    }

    data class Params(
        val reload: Boolean
    )
}