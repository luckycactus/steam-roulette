package ru.luckycactus.steamroulette.domain.about.entity

enum class LicenseType(
    val title: String
) {
    MIT("MIT"),
    Apache2("Apache 2.0"),
    Custom("Custom license")
}