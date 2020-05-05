package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.CoroutineScope
import ru.luckycactus.steamroulette.domain.common.MissingOwnedGamesException
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.SuspendUseCase
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameList
import ru.luckycactus.steamroulette.domain.games.entity.PagingGameListImpl
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import javax.inject.Inject

@Reusable
class GetOwnedGamesPagingListUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<GetOwnedGamesPagingListUseCase.Params, GetOwnedGamesPagingListUseCase.Result>() {

    override suspend fun getResult(params: Params): Result {
        if (!gamesRepository.isUserHasGames(params.steamId)) {
            throw MissingOwnedGamesException()
        }
        val notShownIds = gamesRepository.getVisibleLocalOwnedGamesIds(
            params.steamId,
            params.filter,
            false
        ).shuffled()

        val shownIds = gamesRepository.getVisibleLocalOwnedGamesIds(
            params.steamId,
            params.filter,
            true
        ).shuffled()

        val firstShownGame = shownIds.firstOrNull()
        val gameIds = notShownIds + shownIds
        val pagingList = PagingGameListImpl(
            { gamesRepository.getLocalOwnedGameHeaders(params.steamId, it) },
            gameIds,
            5,
            10,
            params.pagingCoroutineScope
        )

        return Result(pagingList, firstShownGame)
    }

    data class Params(
        val steamId: SteamId,
        val filter: PlaytimeFilter,
        val pagingCoroutineScope: CoroutineScope
    )

    data class Result(
        val pagingList: PagingGameList,
        val firstShownGameId: Int?
    )
}