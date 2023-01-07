package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.CancellationException
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import javax.inject.Inject

@Reusable
class GetGameStoreInfoUseCase @Inject constructor(
    private val gameDetailsRepository: GameDetailsRepository
) {
    suspend operator fun invoke(
        gameId: Int,
        cachePolicy: CachePolicy = CachePolicy.CacheOrRemote
    ): Result {
        return try {
            val gameStoreInfo = gameDetailsRepository.getGameStoreInfo(gameId, cachePolicy)!!
            Result.Success(gameStoreInfo)
        } catch (e: GetGameStoreInfoException) {
            Result.Fail.GameNotFound
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Fail.Error(e)
        }
    }

    sealed class Result {
        data class Success(
            val data: GameStoreInfo
        ) : Result()

        sealed class Fail : Result() {
            object GameNotFound : Fail()

            data class Error(val cause: Exception) : Fail()
        }
    }
}