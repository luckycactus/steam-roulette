package ru.luckycactus.steamroulette.domain.games

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.core.usecase.SuspendUseCase
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameList
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameListImpl
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import javax.inject.Inject

/**
 * ignores hidden and shown fields of GamesFilter
 */
class GetOwnedGamesPagingListUseCase @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val rouletteRepository: RouletteRepository
) : SuspendUseCase<GetOwnedGamesPagingListUseCase.Params, GetOwnedGamesPagingListUseCase.Result>() {

    override suspend fun execute(params: Params): Result {
        try {
            if (!gamesRepository.isUserHasGames()) {
                return Result.Fail.NoOwnedGames
            }

            val topGameId = rouletteRepository.getLastTopGameId()
            var topGameFound = false

            val shownIds = gamesRepository.getOwnedGamesIdsMutable(
                GamesFilter(
                    hidden = false,
                    shown = true,
                    playtime = params.filter.playtime
                ),
                topGameId != null
            )
            if (topGameId != null) {
                val index = shownIds.binarySearch(topGameId)
                if (index >= 0) {
                    topGameFound = true
                    shownIds.removeAt(index)
                }
            }

            val notShownIds = gamesRepository.getOwnedGamesIdsMutable(
                GamesFilter(
                    hidden = false,
                    shown = false,
                    playtime = params.filter.playtime
                ),
                topGameId != null && !topGameFound
            )
            if (topGameId != null && !topGameFound) {
                val index = notShownIds.binarySearch(topGameId)
                if (index >= 0) {
                    topGameFound = true
                    notShownIds.removeAt(index)
                }
            }
            shownIds.shuffle()
            notShownIds.shuffle()

            val firstShownGame = shownIds.firstOrNull()
            val gameIds = if (topGameFound) {
                listOf(topGameId!!) + notShownIds + shownIds
            } else {
                notShownIds + shownIds
            }
            val pagingList = PagingGameListImpl(
                { gamesRepository.getLocalOwnedGameHeaders(it) },
                gameIds,
                5,
                20,
                params.pagingCoroutineScope
            )

            params.pagingCoroutineScope.launch {
                pagingList.observeItemsInsertions().collect {
                    updateTopGame(pagingList)
                }
            }

            params.pagingCoroutineScope.launch {
                pagingList.observeItemsRemovals().collect {
                    updateTopGame(pagingList)
                }
            }

            return Result.Success(pagingList, firstShownGame)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.Fail.Error(e)
        }
    }

    private suspend fun updateTopGame(pagingList: PagingGameList) {
        pagingList.peekTop()?.appId.let { rouletteRepository.setLastTopGameId(it) }
    }

    data class Params(
        val filter: GamesFilter,
        val pagingCoroutineScope: CoroutineScope
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