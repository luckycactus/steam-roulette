package ru.luckycactus.steamroulette.domain.app.migrations

import android.content.SharedPreferences
import androidx.core.content.edit
import ru.luckycactus.steamroulette.data.local.db.AppDatabase
import ru.luckycactus.steamroulette.data.local.db.CacheInfoRoomEntity
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games_filter.RouletteFilterRepository
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.user.GetCurrentUserUseCase
import javax.inject.Inject
import javax.inject.Named

class AppMigration12to13 @Inject constructor(
    private val getCurrentUser: GetCurrentUserUseCase,
    @Named("roulette-filters") private val roulettePrefs: SharedPreferences,
    private val rouletteFilterRepository: RouletteFilterRepository,
    @Named("cache-helper") private val cacheHelperPrefs: SharedPreferences,
    private val db: AppDatabase
) : AppMigration {

    override suspend fun migrate() {
        migrateRouletteFilter()
        migrateCacheHelper()
    }

    private suspend fun migrateRouletteFilter() {
        val steamId = getCurrentUser() ?: return

        val filterType = roulettePrefs.getInt(playTimeKey(steamId), -1)
        val maxHours = roulettePrefs.getInt(maximumPlayTimeKey(steamId), -1)

        val playTimeFilter = when (filterType) {
            0 -> PlaytimeFilter.All
            1 -> PlaytimeFilter.NotPlayed
            2 -> PlaytimeFilter.Limited(maxHours)
            else -> null
        }
        playTimeFilter?.let {
            rouletteFilterRepository.saveFilter(GamesFilter(playtime = playTimeFilter))
        }
        roulettePrefs.edit {
            if (maxHours >= 0) {
                putInt("filter-max-hours-${steamId.as64()}", maxHours)
            }
            remove(playTimeKey(steamId))
            remove(maximumPlayTimeKey(steamId))
        }
    }

    private suspend fun migrateCacheHelper() {
        val items = cacheHelperPrefs.all.map {
            CacheInfoRoomEntity(it.key, it.value as Long)
        }
        db.cacheInfoDao().insert(items)
        cacheHelperPrefs.edit { clear() }
    }

    private fun playTimeKey(steamId: SteamId) = "playTimeFilter-${steamId.as64()}"
    private fun maximumPlayTimeKey(steamId: SteamId) = "maximumPlayTime-${steamId.as64()}"
}