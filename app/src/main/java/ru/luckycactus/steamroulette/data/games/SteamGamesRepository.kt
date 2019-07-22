package ru.luckycactus.steamroulette.data.games

import ru.luckycactus.steamroulette.domain.OwnedGame

interface SteamGamesRepository {

    suspend fun getOwnedGames(userId: Long, reload: Boolean): List<OwnedGame>
}
