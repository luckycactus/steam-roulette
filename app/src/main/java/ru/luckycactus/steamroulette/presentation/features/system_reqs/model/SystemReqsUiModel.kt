package ru.luckycactus.steamroulette.presentation.features.system_reqs.model

data class SystemReqsUiModel(
    val platform: String,
    val minimal: String?,
    val recommended: String?
)