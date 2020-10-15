package ru.luckycactus.steamroulette.domain.library

interface LibrarySettingsRepository {
    fun getScale(default: Int): Int
    fun saveScale(scale: Int)
}