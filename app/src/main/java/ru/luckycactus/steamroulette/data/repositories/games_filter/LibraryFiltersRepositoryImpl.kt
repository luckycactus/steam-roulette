package ru.luckycactus.steamroulette.data.repositories.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.luckycactus.steamroulette.data.repositories.games_filter.datasource.GamesFilterDataSource
import ru.luckycactus.steamroulette.data.repositories.games_filter.datasource.PlaytimeFilterDataSource
import ru.luckycactus.steamroulette.domain.games.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.LibraryFiltersRepository
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import javax.inject.Inject
import javax.inject.Named

@Reusable
class LibraryFiltersRepositoryImpl @Inject constructor(
    @Named("library") private val playtimeFilterDataSource: PlaytimeFilterDataSource,
    private val libraryGamesFilterDataSource: GamesFilterDataSource,
    private val userSession: UserSession
) : LibraryFiltersRepository {

    private val currentUser
        get() = userSession.requireCurrentUser()

    override fun observeFilter(default: GamesFilter): Flow<GamesFilter> =
        combine(
            playtimeFilterDataSource.observeFilter(currentUser),
            libraryGamesFilterDataSource.observeHidden(currentUser),
            libraryGamesFilterDataSource.observeShown(currentUser)
        ) { playtimeFilter, hidden, shown ->
            if (playtimeFilter == null)
                default
            else
                GamesFilter(shown, hidden, playtimeFilter)
        }

    override fun observeMaxHours(default: Int): Flow<Int> {
        return  playtimeFilterDataSource.observeMaxHours(currentUser, default)
    }

    override suspend fun saveFilter(filter: GamesFilter) {
        libraryGamesFilterDataSource.save(currentUser, filter.hidden, filter.shown)
        playtimeFilterDataSource.saveFilter(currentUser, filter.playtime)
    }
}