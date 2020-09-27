package ru.luckycactus.steamroulette.domain.review

class AppReviewExceptionsHandler(
    private val appReviewManager: AppReviewManager
) : Thread.UncaughtExceptionHandler {
    private val delegate = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        appReviewManager.notifyAppCrashed()
        delegate?.uncaughtException(t, e)
    }
}