package ru.luckycactus.steamroulette.domain.analytics

data class SelectContentEvent(
    val type: String,
    val itemId: String,
    val params: Map<String, String> = emptyMap()
)