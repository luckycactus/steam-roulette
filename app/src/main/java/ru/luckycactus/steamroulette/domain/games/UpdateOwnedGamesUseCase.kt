package ru.luckycactus.steamroulette.domain.games

import kotlinx.coroutines.CancellationException
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import javax.inject.Inject

class UpdateOwnedGamesUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<UpdateOwnedGamesUseCase.Params, UpdateOwnedGamesUseCase.Result>() {

    override suspend fun execute(params: Params): Result {
        return try {
            gamesRepository.updateOwnedGames(
                if (params.reload) CachePolicy.Remote else CachePolicy.CacheOrRemote
            )
            Result.Success
        } catch (e: GetOwnedGamesPrivacyException) {
            Result.Fail.PrivateProfile
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Fail.Error(e)
        }
    }

    data class Params(
        val reload: Boolean
    )

    sealed class Result {
        object Success : Result()

        sealed class Fail : Result() {
            object PrivateProfile : Fail()

            data class Error(val cause: Exception) : Fail()
        }
    }
}