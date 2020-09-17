package ru.luckycactus.steamroulette.domain.review

import kotlinx.coroutines.flow.Flow

interface AppReviewRepository {
    var appRated: Boolean
    var launchCount: Int
    var lastRequestTime: Long
    var reviewRequestsEnabled: Boolean

    fun observeRatedState(): Flow<Boolean>
}