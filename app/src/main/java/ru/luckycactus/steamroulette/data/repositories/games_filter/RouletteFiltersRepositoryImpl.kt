package ru.luckycactus.steamroulette.data.repositories.games_filter

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import ru.luckycactus.steamroulette.data.repositories.games_filter.datasource.PlaytimeFilterDataSource
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.user.entity.UserSession
import ru.luckycactus.steamroulette.domain.games_filter.RouletteFiltersRepository
import javax.inject.Inject
import javax.inject.Named

@Reusable
class RouletteFiltersRepositoryImpl @Inject constructor(
    @Named("roulette") private val playtimeFilterDataSource: PlaytimeFilterDataSource,
    private val userSession: UserSession
) : RouletteFiltersRepository {

    private val currentUser
        get() = userSession.requireCurrentUser()

    override fun observePlaytimeFilterType(default: PlaytimeFilter.Type): Flow<PlaytimeFilter.Type> =
        playtimeFilterDataSource.observeFilterType(currentUser, default)

    override fun observeMaxPlaytime(default: Int): Flow<Int> =
        playtimeFilterDataSource.observeMaxHours(currentUser, default)

    override fun observePlaytimeFilter(
        defaultType: PlaytimeFilter.Type,
        defaultMaxHours: Int
    ): Flow<PlaytimeFilter> =
        playtimeFilterDataSource.observeFilter(currentUser, defaultType, defaultMaxHours)

    override fun savePlaytimeFilter(filter: PlaytimeFilter) {
        playtimeFilterDataSource.saveFilter(currentUser, filter)
    }

    override fun clearUser(steamId: SteamId) {
        playtimeFilterDataSource.clear(steamId)
    }

    /**
     * Migration of old EnPlayTimeFilter to new PlaytimeFilter.Type
     */
    override fun migrateEnPlayTimeFilter() {
        playtimeFilterDataSource.migrateEnPlayTimeFilter(currentUser)
    }
}