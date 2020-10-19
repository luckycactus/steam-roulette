package ru.luckycactus.steamroulette.domain.common

interface ImageCacheCleaner {
    suspend fun clearAllCache()
    suspend fun clearMemoryCache()
    suspend fun clearDiskCache()
}