package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractSuspendUseCase
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameList
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameListImpl
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import javax.inject.Inject

@Reusable
class GetOwnedGamesPagingListUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : AbstractSuspendUseCase<GetOwnedGamesPagingListUseCase.Params, GetOwnedGamesPagingListUseCase.Result>() {

    override suspend fun execute(params: Params): Result {
        try {
            if (!gamesRepository.isUserHasGames()) {
                return Result.Fail.NoOwnedGames
            }

            val notShownIds = gamesRepository.getVisibleLocalOwnedGamesIds(
                params.filter,
                false
            ).shuffled()

            val shownIds = gamesRepository.getVisibleLocalOwnedGamesIds(
                params.filter,
                true
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
        val filter: PlaytimeFilter,
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