package ru.luckycactus.steamroulette.domain.entity

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.games.GamesRepository

interface OwnedGamesQueue {
    fun hasNext(): Boolean

    fun markCurrentAsHidden()

    suspend fun next(): OwnedGame

    val size: Int
}

class OwnedGamesQueueImpl(
    private val steamId: SteamId,
    numbers: List<Int>,
    private val gamesRepository: GamesRepository
) : OwnedGamesQueue {
    private val gameIds = numbers.shuffled()

    private var currentIndex = -1

    override val size
        get() = gameIds.size

    override fun hasNext(): Boolean {
        return size - currentIndex - 1 > 0
    }

    override suspend fun next(): OwnedGame {
        if (!hasNext())
            throw NoSuchElementException()
        currentIndex++
        return gamesRepository.getLocalOwnedGame(steamId, gameIds[currentIndex])
    }

    override fun markCurrentAsHidden() {
        check(currentIndex >= 0) { "Cannot mark current game as hidden. You should call next() first!" }
        GlobalScope.launch {
            gamesRepository.markLocalGameAsHidden(steamId, gameIds[currentIndex])
        }
    }
}