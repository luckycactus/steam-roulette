package ru.luckycactus.steamroulette.domain.app

interface SyncGamesPeriodicJob {
    fun start(restart: Boolean = false)
    fun stop()
}