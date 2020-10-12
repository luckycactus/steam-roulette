package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameList
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameListImpl
import javax.inject.Inject

@Reusable
/**
 * ignores hidden and shown fields of GamesFilter
 */
class GetOwnedGamesPagingListUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<GetOwnedGamesPagingListUseCase.Params, GetOwnedGamesPagingListUseCase.Result>() {

    override suspend fun execute(params: Params): Result {
        try {
            if (!gamesRepository.isUserHasGames()) {
                return Result.Fail.NoOwnedGames
            }

            val notShownIds = gamesRepository.getOwnedGamesIds(
                GamesFilter(
                    hidden = false,
                    shown = false,
                    playtime = params.filter.playtime
                )
            ).shuffled()

            val shownIds = gamesRepository.getOwnedGamesIds(
                GamesFilter(
                    hidden = false,
                    shown = true,
                    playtime = params.filter.playtime
                )
            ).shuffled()

            val firstShownGame = shownIds.firstOrNull()
            val gameIds = notShownIds + shownIds
            val pagingList = PagingGameListImpl(
                { gamesRepository.getLocalOwnedGameHeaders(it) },
                gameIds,
                5,
                10,
                params.pagingCoroutineScope
            )

            return Result.Success(pagingList, firstShownGame)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.Fail.Error(e)
        }
    }

    data class Params(
        val filter: GamesFilter,
        val pagingCoroutineScope: CoroutineScope
    )

    sealed class Result {

        data class Success(
            val pagingList: PagingGameList,
            val firstShownGameId: Int?
        ) : Result()

        sealed class Fail : Result() {
            object NoOwnedGames : Fail()

            data class Error(val cause: Exception) : Fail()
        }
    }
}