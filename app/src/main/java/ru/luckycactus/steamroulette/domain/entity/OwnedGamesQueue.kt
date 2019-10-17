package ru.luckycactus.steamroulette.domain.entity

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.games.GamesRepository

interface OwnedGamesQueue {
    fun hasNext(): Boolean

    fun markCurrentAsHidden()

    suspend fun next(): OwnedGame

    fun peekNext(): OwnedGame?

    val size: Int

    val started: Boolean
}

class OwnedGamesQueueImpl(
    private val steamId: SteamId,
    numbers: List<Int>,
    private val gamesRepository: GamesRepository
) : OwnedGamesQueue {
    private val gameIds = numbers.shuffled()

    private var currentIndex = -1
    private var next: OwnedGame? = null

    override val size
        get() = gameIds.size

    override val started: Boolean
        get() = currentIndex >= 0

    override fun hasNext(): Boolean {
        return size - currentIndex - 1 > 0
    }

    override suspend fun next(): OwnedGame {
        if (!hasNext())
            throw NoSuchElementException()
        currentIndex++
        if (currentIndex == 0)
            next = gamesRepository.getLocalOwnedGame(steamId, gameIds[currentIndex])
        val current = next
        if (hasNext())
            next = gamesRepository.getLocalOwnedGame(steamId, gameIds[currentIndex + 1])
        return current!!
    }

    override fun peekNext(): OwnedGame? {
        check(currentIndex >= 0) { "Cannot peek next game. You should call next() first!" }
        return next
    }

    override fun markCurrentAsHidden() {
        check(currentIndex >= 0) { "Cannot mark current game as hidden. You should call next() first!" }
        GlobalScope.launch {
            gamesRepository.markLocalGameAsHidden(steamId, gameIds[currentIndex])
        }
    }
}