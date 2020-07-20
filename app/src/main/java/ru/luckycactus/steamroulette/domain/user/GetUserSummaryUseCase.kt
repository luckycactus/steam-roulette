package ru.luckycactus.steamroulette.domain.user

import dagger.Reusable
import kotlinx.coroutines.CancellationException
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractSuspendUseCase
import ru.luckycactus.steamroulette.domain.user.entity.UserSummary
import javax.inject.Inject

//todo user naming
@Reusable
class GetUserSummaryUseCase @Inject constructor(
    private val userRepository: UserRepository
) : AbstractSuspendUseCase<GetUserSummaryUseCase.Params, GetUserSummaryUseCase.Result>() {

    override suspend fun execute(params: Params): Result =
        try {
            Result.Success(
                userRepository.getUserSummary(
                    params.steamId,
                    if (params.reload) CachePolicy.Remote else CachePolicy.CacheOrRemote
                )!!
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: SteamIdNotFoundException) {
            Result.Fail.SteamIdNotFound
        } catch (e: Exception) {
            Result.Fail.Error(e)
        }

    data class Params(
        val steamId: SteamId,
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
