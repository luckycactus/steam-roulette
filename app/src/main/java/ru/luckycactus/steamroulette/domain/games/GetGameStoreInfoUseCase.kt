package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.CancellationException
import ru.luckycactus.steamroulette.domain.core.CachePolicy
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameStoreInfo
import javax.inject.Inject

@Reusable
class GetGameStoreInfoUseCase @Inject constructor(
    private val gameDetailsRepository: GameDetailsRepository
) : SuspendUseCase<GetGameStoreInfoUseCase.Params, GetGameStoreInfoUseCase.Result>() {

    override suspend fun execute(params: Params): Result {
        return try {
            val gameStoreInfo = gameDetailsRepository.getGameStoreInfo(
                params.gameId,
                params.cachePolicy
            )!!
            Result.Success(gameStoreInfo)
        } catch (e: GetGameStoreInfoException) {
            Result.Fail.GameNotFound
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Fail.Error(e)
        }
    }

    data class Params(
        val gameId: Int,
        val cachePolicy: CachePolicy = CachePolicy.CacheOrRemote
    )

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