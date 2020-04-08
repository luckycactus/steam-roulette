package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.UseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import javax.inject.Inject

@Reusable
class GetHiddenGamesPagedListUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
): UseCase<SteamId, LiveData<PagedList<GameHeader>>>() {
    override fun getResult(params: SteamId): LiveData<PagedList<GameHeader>> {
        return gamesRepository.getHiddenGamesPagedListLiveData(params)
    }
}