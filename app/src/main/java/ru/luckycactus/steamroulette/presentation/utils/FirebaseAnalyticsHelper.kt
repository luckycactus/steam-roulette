package ru.luckycactus.steamroulette.presentation.utils

import android.content.Context
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
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
import ru.luckycactus.steamroulette.domain.common.SteamId
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.games_filter.ObserveLibraryFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.ObserveRouletteFilterUseCase
import ru.luckycactus.steamroulette.domain.games_filter.entity.GamesFilter
import ru.luckycactus.steamroulette.domain.games_filter.entity.PlaytimeFilter
import ru.luckycactus.steamroulette.domain.login.LoginUseCase
import ru.luckycactus.steamroulette.domain.user.ObserveCurrentUserSteamIdUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeRouletteFilter: ObserveRouletteFilterUseCase,
    private val observeLibraryFilter: ObserveLibraryFilterUseCase,
    observeCurrentUserSteamId: ObserveCurrentUserSteamIdUseCase,
    @AppCoScope private val appScope: CoroutineScope
) : AnalyticsHelper {

    private val analytics = FirebaseAnalytics.getInstance(context)

    private val currentUser = observeCurrentUserSteamId()
    private var isLoggingOut: Boolean = false

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

    override fun logScreenIfVisibleAndResumed(fragment: Fragment, screen: String) {
        if (fragment.isResumed
            && fragment.isAdded
            && !fragment.isHidden
            && fragment.view?.visibility == View.VISIBLE
        ) {
            analytics.logEvent(Event.SCREEN_VIEW) {
                param(Param.SCREEN_CLASS, screen)
            }
            Log.d("Analytics", "Screen recorded: $screen")
        }
    }

    override fun logSelectContent(type: String, itemId: String) {
        analytics.logEvent(Event.SELECT_CONTENT) {
            param(Param.CONTENT_TYPE, type)
            param(Param.ITEM_ID, itemId)
        }
        Log.d("Analytics", "Event recorded for $type, $itemId")
    }

    override fun logClick(button: String) {
        analytics.logEvent(Event.SELECT_CONTENT) {
            param(Param.CONTENT_TYPE, "button")
            param(Param.ITEM_ID, button)
        }
        Log.d("Analytics", "Event recorded for click: $button")
    }

    override fun logLoginAttempt(it: LoginUseCase.Result) {
        val loginMethod =
            if (it.steamIdFormat != null && it.steamIdFormat != SteamId.Format.Invalid) {
                it.steamIdFormat.name
            } else if (it.vanityUrlFormat != null && it.vanityUrlFormat != SteamId.VanityUrlFormat.Invalid) {
                "Vanity: ${it.vanityUrlFormat}"
            } else {
                "error"
            }
        analytics.logEvent(Event.SELECT_CONTENT) {
            param(Param.CONTENT_TYPE, "Login attempt")
            param(Param.ITEM_ID, loginMethod)
            param(
                "result",
                if (it is LoginUseCase.Result.Success) "success" else it::class.simpleName!!
            )
        }
        Log.d("Analytics", "Login attempt recorded for $it")
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
        Log.d(
            "Analytics",
            "Games filter change recorded for $filter in ${if (roulette) "roulette" else "library"}"
        )
    }

    companion object {
        private const val devId = 76561198043933405L
    }
}