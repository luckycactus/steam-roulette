package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import kotlinx.coroutines.CancellationException
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import javax.inject.Inject

@Reusable
class GetUserSummaryUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val userSession: UserSession
) {
    suspend operator fun invoke(steamId: SteamId?, reload: Boolean): Result =
        try {
            val user = steamId ?: userSession.requireCurrentUser()
            val cachePolicy = if (reload) CachePolicy.Remote else CachePolicy.CacheOrRemote
            val userSummary = userRepository.getUserSummaryOrThrow(user, cachePolicy)
            Result.Success(userSummary)
        } catch (e: CancellationException) {
            throw e
        } catch (e: SteamIdNotFoundException) {
            Result.Fail.SteamIdNotFound
        } catch (e: Exception) {
            Result.Fail.Error(e)
        }

    sealed class Result {
        data class Success(
            val userSummary: UserSummary
        ) : Result()

        sealed class Fail : Result() {
            object SteamIdNotFound : Fail()

            data class Error(val exception: Exception) : Fail()
        }
    }
}
