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

    override suspend fun execute(params: Params): Result = Impl(params).execute()

    private inner class Impl(
        private val params: Params
    ) {
        private var topGameId: Int? = null
        private var topGameFound = false

        suspend fun execute(): Result {
            try {
                if (!gamesRepository.isUserHasGames()) {
                    return Result.Fail.NoOwnedGames
                }

                topGameId = rouletteRepository.getLastTopGameId()

                val shownIds = getFilteredGames(shown = true)
                findAndRemoveTopGame(shownIds)
                val notShownIds = getFilteredGames(shown = false)
                findAndRemoveTopGame(notShownIds)

                shownIds.shuffle()
                notShownIds.shuffle()

                val firstShownGame = shownIds.firstOrNull()

                val gameIds = ArrayList<Int>(shownIds.size + notShownIds.size + 1).apply {
                    if (topGameFound) {
                        add(topGameId!!)
                    }
                    addAll(notShownIds)
                    addAll(shownIds)
                }

                val pagingList = PagingGameListImpl(
                    { gamesRepository.getLocalOwnedGameHeaders(it) },
                    gameIds,
                    5,
                    20,
                    params.pagingCoroutineScope
                )

                setupLastTopGameUpdates(pagingList)

                return Result.Success(pagingList, firstShownGame)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return Result.Fail.Error(e)
            }
        }

        private suspend fun getFilteredGames(shown: Boolean): MutableList<Int> {
            return gamesRepository.getOwnedGamesIdsMutable(
                GamesFilter(
                    hidden = false,
                    shown = shown,
                    playtime = params.filter.playtime
                ),
                topGameId != null && !topGameFound
            )
        }

        private fun findAndRemoveTopGame(games: MutableList<Int>): MutableList<Int> {
            if (topGameId != null && !topGameFound) {
                val index = games.binarySearch(topGameId)
                if (index >= 0) {
                    topGameFound = true
                    games.removeAt(index)
                }
            }
            return games
        }

        private fun setupLastTopGameUpdates(pagingList: PagingGameList) {
            params.pagingCoroutineScope.launch {
                pagingList.topGameFlow.collect {
                    it?.let { rouletteRepository.setLastTopGameId(it.appId) }
                }
            }
        }

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