package ru.luckycactus.steamroulette.domain.entity

interface GameCoverCacheCleaner {
    suspend fun clearAllCache()
}