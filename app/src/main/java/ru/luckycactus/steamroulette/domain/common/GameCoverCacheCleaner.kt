package ru.luckycactus.steamroulette.domain.common

interface GameCoverCacheCleaner {
    suspend fun clearAllCache()
}