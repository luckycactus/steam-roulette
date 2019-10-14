package ru.luckycactus.steamroulette.domain.entity

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.presentation.common.App
import java.lang.IllegalStateException
import java.util.*
import kotlin.NoSuchElementException

interface OwnedGamesQueue {

    fun hasNext(): Boolean

    fun markCurrentAsHidden()

    suspend fun next(): OwnedGame
}

class OwnedGamesQueueImpl(
    private val steamId: SteamId,
    numbers: List<Int>,
    private val gamesRepository: GamesRepository
) : OwnedGamesQueue {
    private val numberQueue = ArrayDeque(numbers.shuffled())

    private var currentMarkedAsHidden = false
    private var current: OwnedGame? = null

    init {
        GlobalScope.launch {
            for (i in 0 until minOf(5, numberQueue.size - 1)) {
                val n = numberQueue.elementAt(i)
                Glide.with(App.getInstance())
                    .load(gamesRepository.getLocalOwnedGame(steamId, n).libraryPortraitImageUrlHD)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .preload()
            }
        }

    }

    override fun hasNext(): Boolean {
        return numberQueue.size - (if (current != null) 1 else 0) > 0
    }

    override suspend fun next(): OwnedGame {
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
        checkNotNull(current) { "Cannot mark current game as hidden. You should call next() first!" }
        currentMarkedAsHidden = true
        GlobalScope.launch {
            gamesRepository.markLocalGameAsHidden(steamId, current!!)
        }
    }
}