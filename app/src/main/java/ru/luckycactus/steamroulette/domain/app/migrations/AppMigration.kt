package ru.luckycactus.steamroulette.domain.app.migrations

interface AppMigration {
    suspend fun migrate()
}