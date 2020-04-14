package ru.luckycactus.steamroulette.domain.about.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppLibrary(
    @Json(name = "author") val author: String,
    @Json(name = "name") val name: String,
    @Json(name = "source") val sourceUrl: String,
    @Json(name = "license_type") val licenseType: LicenseType
)