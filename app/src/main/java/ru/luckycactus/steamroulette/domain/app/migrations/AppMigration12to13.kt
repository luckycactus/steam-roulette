package ru.luckycactus.steamroulette.domain.app.migrations

import android.content.SharedPreferences
import androidx.core.content.edit
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
    @Named("roulette-filters") private val prefs: SharedPreferences,
    private val rouletteFilterRepository: RouletteFilterRepository
) : AppMigration {

    override suspend fun migrate() {
        val steamId = getCurrentUser() ?: return

        val filterType = prefs.getInt(playTimeKey(steamId), -1)
        val maxHours = prefs.getInt(maximumPlayTimeKey(steamId), -1)

        val playTimeFilter = when (filterType) {
            0 -> PlaytimeFilter.All
            1 -> PlaytimeFilter.NotPlayed
            2 -> PlaytimeFilter.Limited(maxHours)
            else -> null
        }
        playTimeFilter?.let {
            rouletteFilterRepository.saveFilter(GamesFilter(playtime = playTimeFilter))
        }
        prefs.edit {
            if (maxHours >= 0) {
                putInt("filter-max-hours-${steamId.as64()}", maxHours)
            }
            remove(playTimeKey(steamId))
            remove(maximumPlayTimeKey(steamId))
        }
    }

    private fun playTimeKey(steamId: SteamId) = "playTimeFilter-${steamId.as64()}"
    private fun maximumPlayTimeKey(steamId: SteamId) = "maximumPlayTime-${steamId.as64()}"
}