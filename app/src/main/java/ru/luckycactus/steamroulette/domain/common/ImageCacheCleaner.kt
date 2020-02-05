package ru.luckycactus.steamroulette.domain.common

interface ImageCacheCleaner {
    suspend fun clearAllCache()
}