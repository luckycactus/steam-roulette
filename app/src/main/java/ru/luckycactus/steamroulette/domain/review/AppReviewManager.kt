package ru.luckycactus.steamroulette.domain.review

import ru.luckycactus.steamroulette.domain.core.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class AppReviewManager @Inject constructor(
    private val repository: AppReviewRepository,
    private val clock: Clock
) {
    private var sessionWasStarted = false

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

    fun notifySessionStarted() {
        if (isRated() || sessionWasStarted)
            return
        sessionWasStarted = true
        if (repository.launchCount == 0) {
            delayReviewRequest()
        }
        repository.launchCount++
    }

    fun notifyAppCrashed() {
        if (isRated())
            return
        repository.resetLaunchesSynchronously()
    }

    fun delayReviewRequest() {
        repository.lastRequestTime = clock.currentTimeMillis()
    }

    fun setReviewRequestsEnabled(enabled: Boolean) {
        repository.reviewRequestsEnabled = enabled
    }

    companion object {
        private const val MIN_LAUNCH_COUNT = 5
        private val REQUEST_DELAY = 2.days
    }
}