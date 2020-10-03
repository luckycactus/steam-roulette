package ru.luckycactus.steamroulette.data.repositories.games_filter.datasource

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.luckycactus.steamroulette.data.core.intFlow
import ru.luckycactus.steamroulette.domain.common.SteamId
import javax.inject.Inject
import javax.inject.Named

@Reusable
class LibraryGamesFilterDataSource @Inject constructor(
    @Named("roulette-filters") private val prefs: SharedPreferences,
) : GamesFilterDataSource {

    override fun observeHidden(steamId: SteamId): Flow<Boolean?> {
        return prefs.intFlow(key(steamId, "hidden"), null.asInt()).map { it.asBoolean() }
    }

    override fun observeShown(steamId: SteamId): Flow<Boolean?> {
        return prefs.intFlow(key(steamId, "shown"), null.asInt()).map { it.asBoolean() }
    }

    override fun save(steamId: SteamId, hidden: Boolean?, shown: Boolean?) {
        prefs.edit {
            putInt(key(steamId, "hidden"), hidden.asInt())
            putInt(key(steamId, "shown"), shown.asInt())
        }
    }

    private fun key(steamId: SteamId, pref: String) = "filter-${pref}-${steamId.as64()}"

    //todo library
    private fun Boolean?.asInt() = when (this) {
        null -> 0
        true -> 1
        false -> 2
    }

    private fun Int.asBoolean() = when (this) {
        0 -> null
        1 -> true
        2 -> false
        else -> throw IllegalArgumentException()
    }
}