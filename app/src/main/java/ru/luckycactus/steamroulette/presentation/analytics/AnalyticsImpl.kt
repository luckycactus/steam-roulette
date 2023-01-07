package ru.luckycactus.steamroulette.presentation.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.google.firebase.analytics.FirebaseAnalytics.Param
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.di.AppCoScope
import ru.luckycactus.steamroulette.domain.analytics.Analytics
import ru.luckycactus.steamroulette.domain.analytics.SelectContentEvent
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games_filter.ObserveLibraryFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObserveRouletteFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.user.ObserveCurrentUserSteamIdUseCase
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeRouletteFilter: ObserveRouletteFilterUseCase,
    private val observeLibraryFilter: ObserveLibraryFilterUseCase,
    observeCurrentUserSteamId: ObserveCurrentUserSteamIdUseCase,
    @AppCoScope private val appScope: CoroutineScope
) : Analytics {

    private val analytics = FirebaseAnalytics.getInstance(context)

    private val currentUser = observeCurrentUserSteamId()
    private var isLoggingOut: Boolean = false

    // todo refactor - move to separate usecase
    init {
        appScope.launch {
            currentUser.flatMapLatest {
                it?.let {
                    observeRouletteFilter()
                        .distinctUntilChanged()
                        .filter { !isLoggingOut }
                } ?: emptyFlow()
            }.collect {
                logGamesFilter(it, true)
            }
        }

        appScope.launch {
            currentUser.flatMapLatest {
                it?.let {
                    observeLibraryFilter()
                        .distinctUntilChanged()
                        .filter { !isLoggingOut }
                        .drop(1)
                } ?: emptyFlow()
            }.collect {
                logGamesFilter(it, false)
            }
        }

        appScope.launch {
            currentUser.collect {
                setUserSignedIn(it != null)
                analytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG && it?.as64() != devId)
            }
        }
    }

    override fun trackScreen(screen: String?) {
        if (screen == null) return

        analytics.logEvent(Event.SCREEN_VIEW) {
            param(Param.SCREEN_CLASS, screen)
        }
        Timber.d("Screen recorded: $screen")
    }

    override fun track(event: SelectContentEvent) {
        analytics.logEvent(Event.SELECT_CONTENT) {
            param(Param.CONTENT_TYPE, event.type)
            param(Param.ITEM_ID, event.itemId)
            event.params.forEach { (key, value) ->
                param(key, value)
            }
        }
        Timber.d("Event recorded for ${event.type}, ${event.itemId}")
    }

    override fun setUserIsLoggingOut() {
        isLoggingOut = true
    }

    private fun setUserSignedIn(isSignedIn: Boolean) {
        if (!isSignedIn)
            isLoggingOut = false
        analytics.setUserProperty("user_signed_in", isSignedIn.toString())
    }

    private fun logGamesFilter(filter: GamesFilter, roulette: Boolean) {
        val target = if (roulette) "roulette" else "library"
        val filterName = when {
            filter.hidden == true -> "hidden"
            filter.playtime == PlaytimeFilter.All -> "all"
            filter.playtime == PlaytimeFilter.NotPlayed -> "not played"
            filter.playtime is PlaytimeFilter.Limited -> "limited"
            else -> "unknown"
        }
        analytics.setUserProperty("$target filter", filterName)
        analytics.setUserProperty(
            "$target max hours",
            if (filter.playtime is PlaytimeFilter.Limited) filter.playtime.maxHours.toString() else null
        )
        Timber.d(
            "Games filter change recorded for $filter in ${if (roulette) "roulette" else "library"}"
        )
    }

    companion object {
        private const val devId = 76561198043933405L
    }
}