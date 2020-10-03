package ru.luckycactus.steamroulette.data.repositories.games_filter.datasource

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.data.core.intFlow
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter

//todo library refactor
class LocalPlaytimeFilterDataSource(
    private val filterPrefs: SharedPreferences
) : PlaytimeFilterDataSource {

    override fun observeFilterType(
        steamId: SteamId,
        default: PlaytimeFilter.Type
    ): Flow<PlaytimeFilter.Type> = filterPrefs.intFlow(playTimeKey(steamId), default.ordinal)
        .map { PlaytimeFilter.Type.fromOrdinal(it) }

    override fun observeFilterType(
        steamId: SteamId,
    ): Flow<PlaytimeFilter.Type?> = filterPrefs.intFlow(playTimeKey(steamId), -1)
        .map {
            if (it >= 0)
                PlaytimeFilter.Type.fromOrdinal(it)
            else null
        }

    override fun observeMaxHours(steamId: SteamId, default: Int): Flow<Int> =
        filterPrefs.intFlow(maximumPlayTimeKey(steamId), default)

    override fun observeFilter(
        steamId: SteamId,
        defaultType: PlaytimeFilter.Type,
        defaultMaxHours: Int
    ) = observeFilterType(steamId, defaultType)
        .combine(observeMaxHours(steamId, defaultMaxHours)) { type, maxPlaytime ->
            when (type) {
                PlaytimeFilter.Type.All -> PlaytimeFilter.All
                PlaytimeFilter.Type.NotPlayed -> PlaytimeFilter.NotPlayed
                PlaytimeFilter.Type.Limited -> PlaytimeFilter.Limited(maxPlaytime)
            }
        }

    override fun observeFilter(
        steamId: SteamId
    ) = observeFilterType(steamId)
        .combine(observeMaxHours(steamId, -1)) { type, maxPlaytime ->
            when (type) {
                PlaytimeFilter.Type.All -> PlaytimeFilter.All
                PlaytimeFilter.Type.NotPlayed -> PlaytimeFilter.NotPlayed
                PlaytimeFilter.Type.Limited -> PlaytimeFilter.Limited(maxPlaytime)
                null -> null
            }
        }

    override fun saveFilterType(steamId: SteamId, filterType: PlaytimeFilter.Type) {
        filterPrefs.edit {
            putInt(playTimeKey(steamId), filterType.ordinal)
        }
    }

    override fun saveFilter(steamId: SteamId, filter: PlaytimeFilter) {
        if (filter is PlaytimeFilter.Limited) {
            saveMaxPlaytime(steamId, filter.maxHours)
        }
        saveFilterType(steamId, filter.type)
    }

    override fun clear(steamId: SteamId) {
        filterPrefs.edit {
            remove(playTimeKey(steamId))
            remove(maximumPlayTimeKey(steamId))
        }
    }

    override fun migrateEnPlayTimeFilter(steamId: SteamId) {
        val key = playTimeKey(steamId)
        // EnPlayTimeFilter.NotPlayedIn2Weeks.ordinal == 2
        if (filterPrefs.getInt(key, -1) == 2) {
            saveFilterType(steamId, PlaytimeFilter.Type.All)
        }
    }

    override fun saveMaxPlaytime(steamId: SteamId, maxHours: Int) {
        filterPrefs.edit {
            putInt(maximumPlayTimeKey(steamId), maxHours)
        }
    }

    //todo library unify and migrate
    private fun playTimeKey(steamId: SteamId) = "playTimeFilter-${steamId.as64()}"
    private fun maximumPlayTimeKey(steamId: SteamId) = "maximumPlayTime-${steamId.as64()}"
}