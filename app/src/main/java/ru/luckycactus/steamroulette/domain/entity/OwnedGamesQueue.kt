package ru.luckycactus.steamroulette.domain.entity

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import java.lang.IllegalStateException
import java.util.*
import kotlin.NoSuchElementException

interface OwnedGamesQueue {

    fun hasNext(): Boolean

    fun markCurrentAsHidden()

    suspend fun next(): OwnedGame

    fun invalidate()
}

class OwnedGamesQueueImpl(
    private val steamId: SteamId,
    numbers: List<Int>,
    private val gamesRepository: GamesRepository
) : OwnedGamesQueue {
    private val numberQueue = LinkedList(numbers.shuffled())

    private var currentMarkedAsHidden = false
    private var current: OwnedGame? = null
    private var invalidated = false

    override fun hasNext(): Boolean {
        checkInvalidated()
        return numberQueue.size - (if (current != null) 1 else 0) > 0
    }

    override suspend fun next(): OwnedGame {
        checkInvalidated()
        if (!hasNext())
            throw NoSuchElementException()
        if (current != null) {
            val currentNumber = numberQueue.removeFirst()
            if (!currentMarkedAsHidden) {
                numberQueue.addLast(currentNumber)
            } else {
                currentMarkedAsHidden = false
            }
        }
        return gamesRepository.getLocalOwnedGame(steamId, numberQueue.first).also {
            current = it
        }
    }

    override fun markCurrentAsHidden() {
        checkInvalidated()
        if (current == null)
            throw IllegalStateException("Cannot mark current game as hidden. You should call next() first!")
        currentMarkedAsHidden = true
        GlobalScope.launch {
            gamesRepository.markLocalGameAsHidden(steamId, current!!)
        }
    }

    override fun invalidate() {
        invalidated = true
    }

    private fun checkInvalidated() {
        if (invalidated)
            throw IllegalStateException("Queue was invalidated!")
    }
}