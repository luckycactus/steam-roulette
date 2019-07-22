package ru.luckycactus.steamroulette.data.games.cache

import ru.luckycactus.steamroulette.data.model.OwnedGameEntity

interface SteamGamesCache {

    fun putOwnedGames(games: List<OwnedGameEntity>)

    fun isExpired(): Boolean
}
