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

    override var appRated: Boolean by prefs.boolean("app_rated", false)

    override var launchCount: Int by prefs.int("launch_count", 0)

    override fun resetLaunchesSynchronously() {
        prefs.edit(commit = true) {
            putInt("launch_count", 0)
        }
    }

    override var lastRequestTime: Long by prefs.long("last_request", 0)

    override var reviewRequestsEnabled: Boolean by prefs.boolean("requests_enabled", true)

    override fun observeRatedState() = prefs.booleanFlow("app_rated", false)
}