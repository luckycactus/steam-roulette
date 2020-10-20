package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameList
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameListImpl
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
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

            var topGameFound = false

            val shownIds = gamesRepository.getOwnedGamesIdsMutable(
                GamesFilter(
                    hidden = false,
                    shown = true,
                    playtime = params.filter.playtime
                ),
                params.topGame != null
            )
            if (params.topGame != null) {
                val index = shownIds.binarySearch(params.topGame.appId)
                if (index >= 0) {
                    topGameFound = true
                    shownIds.remove(index)
                }
            }

            val notShownIds = gamesRepository.getOwnedGamesIdsMutable(
                GamesFilter(
                    hidden = false,
                    shown = false,
                    playtime = params.filter.playtime
                ),
                params.topGame != null && !topGameFound
            )
            if (params.topGame != null && !topGameFound) {
                val index = notShownIds.binarySearch(params.topGame.appId)
                if (index >= 0) {
                    topGameFound = true
                    notShownIds.remove(index)
                }
            }
            shownIds.shuffle()
            notShownIds.shuffle()

            val firstShownGame = shownIds.firstOrNull()
            val gameIds = if (topGameFound) {
                listOf(params.topGame!!.appId) + notShownIds + shownIds
            } else {
                notShownIds + shownIds
            }
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
        val pagingCoroutineScope: CoroutineScope,
        val topGame: GameHeader? = null
    )

    sealed class Result {

        data class Success(
            val pagingList: PagingGameList,
            val firstShownGameId: Int?,
        ) : Result()

        sealed class Fail : Result() {
            object NoOwnedGames : Fail()

            data class Error(val cause: Exception) : Fail()
        }
    }
}