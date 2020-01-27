package ru.luckycactus.steamroulette.domain.games

import dagger.Reusable
import kotlinx.coroutines.CoroutineScope
import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.exception.MissingOwnedGamesException
import ru.luckycactus.steamroulette.presentation.features.roulette.PagingGameList
import ru.luckycactus.steamroulette.presentation.features.roulette.PagingGameListImpl
import javax.inject.Inject

@Reusable
class GetOwnedGamesPagingList @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<GetOwnedGamesPagingList.Params, PagingGameList>() {

    override suspend fun getResult(params: Params): PagingGameList {
        if (!gamesRepository.isUserHasGames(params.steamId)) {
            throw MissingOwnedGamesException()
        }
        val gameIds = gamesRepository.getLocalOwnedGamesIds(params.steamId, params.filter)
            .shuffled()
        return PagingGameListImpl(
            { gamesRepository.getLocalOwnedGames(params.steamId, it) },
            gameIds,
            5,
            10,
            params.pagingCoroutineScope
        )
    }

    data class Params(
        val steamId: SteamId,
        val filter: PlaytimeFilter,
        val pagingCoroutineScope: CoroutineScope
    )
}