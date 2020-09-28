package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import kotlinx.coroutines.CancellationException
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import javax.inject.Inject

@Reusable
class GetUserSummaryUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val userSession: UserSession
) : SuspendUseCase<GetUserSummaryUseCase.Params, GetUserSummaryUseCase.Result>() {

    override suspend fun execute(params: Params): Result =
        try {
            val user = params.steamId ?: userSession.requireCurrentUser()
            Result.Success(
                userRepository.getUserSummaryOrThrow(
                    user,
                    if (params.reload) CachePolicy.Remote else CachePolicy.CacheOrRemote
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: SteamIdNotFoundException) {
            Result.Fail.SteamIdNotFound
        } catch (e: Exception) {
            Result.Fail.Error(e)
        }

    class Params(
        val steamId: SteamId? = null,
        val reload: Boolean
    )

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
