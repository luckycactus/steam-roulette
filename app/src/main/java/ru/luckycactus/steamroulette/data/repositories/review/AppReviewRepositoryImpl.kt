package ru.luckycactus.steamroulette.data.repositories.review

import android.content.SharedPreferences
import androidx.core.content.edit
import ru.luckycactus.steamroulette.data.core.boolean
import ru.luckycactus.steamroulette.data.core.booleanFlow
import ru.luckycactus.steamroulette.data.core.int
import ru.luckycactus.steamroulette.data.core.long
import ru.luckycactus.steamroulette.domain.review.AppReviewRepository
import javax.inject.Inject
import javax.inject.Named

class AppReviewRepositoryImpl @Inject constructor(
    @Named("app-review") private val prefs: SharedPreferences
) : AppReviewRepository {

    override var appRated: Boolean by prefs.boolean(false, "app_rated")

    override var launchCount: Int by prefs.int(0, "launch_count")

    override fun resetLaunchesSynchronously() {
        prefs.edit(commit = true) {
            putInt("launch_count", 0)
        }
    }

    override var lastRequestTime: Long by prefs.long(0, "last_request")

    override var reviewRequestsEnabled: Boolean by prefs.boolean(true, "requests_enabled")

    override fun observeRatedState() = prefs.booleanFlow(false, "app_rated")
}