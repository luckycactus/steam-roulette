package ru.luckycactus.steamroulette.domain.common

interface LanguageProvider {
    fun getLanguageForStoreApi(): String
    fun getLanguageForWebApi(): String
}