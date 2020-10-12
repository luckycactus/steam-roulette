package ru.luckycactus.steamroulette.presentation.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements
import ru.luckycactus.steamroulette.presentation.features.about.AboutFragment
import ru.luckycactus.steamroulette.presentation.features.about.AppLibrariesFragment
import ru.luckycactus.steamroulette.presentation.features.detailed_description.DetailedDescriptionFragment
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsFragment
import ru.luckycactus.steamroulette.presentation.features.games.LibraryFragment
import ru.luckycactus.steamroulette.presentation.features.login.LoginFragment
import ru.luckycactus.steamroulette.presentation.features.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.features.system_reqs.SystemReqsFragment
import ru.luckycactus.steamroulette.presentation.utils.customtabs.CustomTabsHelper
import ru.luckycactus.steamroulette.presentation.utils.getThemeColorOrThrow
import ru.luckycactus.steamroulette.presentation.utils.isAppInstalled
import ru.terrakok.cicerone.android.support.SupportAppScreen

sealed class Screens : SupportAppScreen() {

    object Login : Screens() {
        override fun getFragment(): Fragment = LoginFragment.newInstance()
    }

    object Roulette : Screens() {
        override fun getFragment(): Fragment = RouletteFragment.newInstance()
    }

    data class GameDetails(
        val game: GameHeader,
        val color: Int,
        val waitForImage: Boolean
    ) : Screens() {
        override fun getFragment(): Fragment =
            GameDetailsFragment.newInstance(game, color, waitForImage)
    }

    data class SystemReqs(
        val appName: String,
        val systemReqs: List<SystemRequirements>
    ) : Screens() {
        override fun getFragment(): Fragment = SystemReqsFragment.newInstance(appName, systemReqs)
    }

    data class DetailedDescription(
        val appName: String,
        val detailedDescription: String
    ) : Screens() {
        override fun getFragment(): Fragment =
            DetailedDescriptionFragment.newInstance(appName, detailedDescription)
    }

    object HiddenGames : Screens() {
        override fun getFragment(): Fragment = LibraryFragment.newInstance()
    }

    object About : Screens() {
        override fun getFragment(): Fragment = AboutFragment.newInstance()
    }

    object UsedLibraries : Screens() {
        override fun getFragment(): Fragment = AppLibrariesFragment.newInstance()
    }

    object Library : Screens() {
        override fun getFragment(): Fragment? = LibraryFragment.newInstance()
    }

    data class ExternalBrowserFlow(
        val url: String,
        val trySteamApp: Boolean = false
    ) : Screens() {

        override fun getActivityIntent(context: Context): Intent {
            if (trySteamApp) {
                val intent = getSteamAppIntent(context)
                if (intent != null)
                    return intent
            }
            return if (CustomTabsHelper.isCustomTabsSupported(context)) {
                CustomTabsIntent.Builder().apply {
                    setToolbarColor(context.getThemeColorOrThrow(R.attr.colorSurface))
                    setSecondaryToolbarColor(context.getThemeColorOrThrow(R.attr.colorSurface))
                    setExitAnimations(
                        context,
                        R.anim.anim_fragment_pop_enter,
                        R.anim.anim_fragment_pop_exit
                    )
                    setNavigationBarColor(context.getThemeColorOrThrow(R.attr.colorSurface))
                }.build().intent.apply {
                    data = Uri.parse(url)
                }
            } else {
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            }
        }

        private fun getSteamAppIntent(context: Context): Intent? {
            if (isAppInstalled(context, "com.valvesoftware.android.steam.community")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                with(intent) {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    `package` = "com.valvesoftware.android.steam.community"
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    return intent
                }
            }
            return null
        }
    }
}