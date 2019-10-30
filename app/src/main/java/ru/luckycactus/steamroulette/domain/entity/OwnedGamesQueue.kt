package ru.luckycactus.steamroulette.domain.entity

import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import java.util.*
import kotlin.Comparator
import kotlin.NoSuchElementException
import kotlin.coroutines.coroutineContext


interface OwnedGamesQueue {
    fun hasNext(): Boolean

    fun markCurrentAsHidden()

    suspend fun next(): OwnedGame

    fun peekNext(): OwnedGame?

    val size: Int

    val started: Boolean

    fun finish()
}

class OwnedGamesQueueImpl(
    private val steamId: SteamId,
    numbers: List<Int>,
    private val gamesRepository: GamesRepository,
    private val gameCoverPreloader: GameCoverPreloader
) : OwnedGamesQueue {
    private val gameIds = numbers.shuffled()

    private var currentIndex = -1
    private val buffer = ArrayDeque<Pair<OwnedGame, Target<*>>>(BUFFER_SIZE)
    private var currentTarget: Target<*>? = null

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
        return if (currentIndex == 0) {
            val nextIds = gameIds.subList(0, minOf(gameIds.size, BUFFER_SIZE + 1))
            val nextElements =
                gamesRepository.getLocalOwnedGames(steamId, nextIds)
                    .sortedWith(Comparator { o1, o2 ->
                        nextIds.indexOf(o1.appId) - nextIds.indexOf(o2.appId)
                    })
            for (game in nextElements.subList(1, nextElements.size)) {
                addToBuffer(game)
            }
            nextElements[0]
        } else {
            currentTarget?.let { gameCoverPreloader.cancelPreload(it) }
            val current = buffer.removeFirst()
            if (currentIndex + BUFFER_SIZE < gameIds.size) {
                val next = gamesRepository.getLocalOwnedGame(
                    steamId,
                    gameIds[currentIndex + BUFFER_SIZE]
                )
                addToBuffer(next)
            }
            currentTarget = current.second
            current.first
        }

    }

    override fun peekNext(): OwnedGame? {
        check(currentIndex >= 0) { "Cannot peek next game. You should call next() first!" }
        return buffer.peekFirst()?.first
    }

    override fun markCurrentAsHidden() {
        check(currentIndex >= 0) { "Cannot mark current game as hidden. You should call next() first!" }
        val gameId = gameIds[currentIndex]
        GlobalScope.launch {
            gamesRepository.markLocalGameAsHidden(steamId, gameId)
        }
    }

    override fun finish() {
        currentTarget?.let { gameCoverPreloader.cancelPreload(it) }
        buffer.forEach {
            gameCoverPreloader.cancelPreload(it.second)
        }
    }

    private fun addToBuffer(game: OwnedGame) {
        buffer.addLast(game to gameCoverPreloader.preload(game))
    }

    companion object {
        private const val BUFFER_SIZE = 2
    }
}