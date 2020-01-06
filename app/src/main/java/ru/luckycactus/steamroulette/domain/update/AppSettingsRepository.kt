package ru.luckycactus.steamroulette.domain.update

interface AppSettingsRepository {

    var lastVersion: Int

    val currentVersion: Int
}