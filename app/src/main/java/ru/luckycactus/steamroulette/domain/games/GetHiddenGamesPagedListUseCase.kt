package ru.luckycactus.steamroulette.domain.games

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import dagger.Reusable
import ru.luckycactus.steamroulette.domain.core.usecase.AbstractUseCase
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import javax.inject.Inject

@Reusable
class GetHiddenGamesPagedListUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) : AbstractUseCase<Unit, LiveData<PagedList<GameHeader>>>() {
    override fun execute(params: Unit): LiveData<PagedList<GameHeader>> {
        return gamesRepository.getHiddenGamesPagedListLiveData()
    }
}