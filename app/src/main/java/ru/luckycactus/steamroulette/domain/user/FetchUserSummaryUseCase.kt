package ru.luckycactus.steamroulette.domain.user

import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.utils.extensions.cancellable
import javax.inject.Inject

class FetchUserSummaryUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(reload: Boolean): Result<Unit> {
        val cachePolicy = if (reload) CachePolicy.Remote else CachePolicy.CacheOrRemote
        return kotlin.runCatching {
            userRepository.fetchUserSummary(cachePolicy)
        }.cancellable()
    }
}