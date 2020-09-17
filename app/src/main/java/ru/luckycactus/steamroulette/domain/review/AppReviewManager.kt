package ru.luckycactus.steamroulette.domain.review

import ru.luckycactus.steamroulette.domain.core.Clock
import javax.inject.Inject
import kotlin.time.days
import kotlin.time.milliseconds

class AppReviewManager @Inject constructor(
    private val repository: AppReviewRepository,
    private val clock: Clock
) {
    fun isRated() = repository.appRated

    fun setRated(appIsRated: Boolean) {
        repository.appRated = appIsRated
    }

    fun observeRatedState() = repository.observeRatedState()

    fun shouldRequestForReview() =
        !isRated()
                && repository.reviewRequestsEnabled
                && repository.launchCount >= MIN_LAUNCH_COUNT
                && (clock.currentTimeMillis() - repository.lastRequestTime).milliseconds > REQUEST_DELAY

    fun incrementLaunchCount() {
        if (repository.launchCount == 0) {
            delayReviewRequest()
        }
        repository.launchCount++
    }

    fun delayReviewRequest() {
        repository.lastRequestTime = clock.currentTimeMillis()
    }

    fun setReviewRequestsEnabled(enabled: Boolean) {
        repository.reviewRequestsEnabled = enabled
    }

    companion object {
        private const val MIN_LAUNCH_COUNT = 6
        private val REQUEST_DELAY = 2.days
    }
}