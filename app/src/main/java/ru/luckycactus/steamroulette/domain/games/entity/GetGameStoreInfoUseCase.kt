package ru.luckycactus.steamroulette.domain.games.entity

import ru.luckycactus.steamroulette.domain.common.SuspendUseCase
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import javax.inject.Inject

class GetGameStoreInfoUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : SuspendUseCase<GetGameStoreInfoUseCase.Params, GameStoreInfo>() {

    override suspend fun getResult(params: Params): GameStoreInfo {
        return gamesRepository.getGameStoreInfo(params.gameId, params.reload)
    }

    data class Params(
        val gameId: Int,
        val reload: Boolean
    )
}