package ru.luckycactus.steamroulette.domain.games

import kotlinx.coroutines.CancellationException
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import javax.inject.Inject

class UpdateOwnedGamesUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    suspend operator fun invoke(reload: Boolean): Result {
        return try {
            val cachePolicy = if (reload) CachePolicy.Remote else CachePolicy.CacheOrRemote
            gamesRepository.updateOwnedGames(cachePolicy)
            Result.Success
        } catch (e: GetOwnedGamesPrivacyException) {
            Result.Fail.PrivateProfile
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Fail.Error(e)
        }
    }

    sealed class Result {
        object Success : Result()

        sealed class Fail : Result() {
            object PrivateProfile : Fail()

            data class Error(val cause: Exception) : Fail()
        }
    }
}