package ru.luckycactus.steamroulette.presentation.navigation

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityOptionsCompat
import androidx.core.net.toUri
import com.github.terrakok.cicerone.androidx.ActivityScreen
import com.github.terrakok.cicerone.androidx.Creator
import com.github.terrakok.cicerone.androidx.FragmentScreen
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.features.about.AboutFragment
import ru.luckycactus.steamroulette.presentation.features.about.AppLibrariesFragment
import ru.luckycactus.steamroulette.presentation.features.detailed_description.DetailedDescriptionFragment
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsFragment
import ru.luckycactus.steamroulette.presentation.features.games.LibraryFragment
import ru.luckycactus.steamroulette.presentation.features.login.LoginFragment
import ru.luckycactus.steamroulette.presentation.features.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.features.system_reqs.SystemReqsFragment
import ru.luckycactus.steamroulette.presentation.utils.extensions.getThemeColorOrThrow
import ru.luckycactus.steamroulette.presentation.utils.isAppInstalled

object Screens {

    fun Login() = FragmentScreen { LoginFragment.newInstance() }

    fun Roulette() = FragmentScreen { RouletteFragment.newInstance() }

    fun GameDetails(
        game: GameHeader,
        color: Int,
        waitForImage: Boolean
    ) = FragmentScreen { GameDetailsFragment.newInstance(game, color, waitForImage) }

    fun SystemReqs(
        appName: String,
        systemReqs: List<SystemRequirements>
    ) = FragmentScreen { SystemReqsFragment.newInstance(appName, systemReqs) }

    fun DetailedDescription(
        appName: String,
        detailedDescription: String
    ) = FragmentScreen { DetailedDescriptionFragment.newInstance(appName, detailedDescription) }

    fun About() = FragmentScreen { AboutFragment.newInstance() }

    fun UsedLibraries() = FragmentScreen { AppLibrariesFragment.newInstance() }

    fun Library() = FragmentScreen { LibraryFragment.newInstance() }

    fun HiddenGames() = FragmentScreen { LibraryFragment.newInstance(true) }

    fun ExternalBrowserFlow(
        url: String,
        trySteamApp: Boolean = false
    ) = ActivityScreen(
        startActivityOptions = activityOptions,
        intentCreator = object : Creator<Context, Intent> {

            override fun create(argument: Context): Intent {
                if (trySteamApp) {
                    val intent = getSteamAppIntent(argument)
                    if (intent != null)
                        return intent
                }
                return createCustomTabsIntent(argument)
            }

            fun createCustomTabsIntent(context: Context): Intent {
                return CustomTabsIntent.Builder().apply {
                    val defaultParams = CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(context.getThemeColorOrThrow(com.google.android.material.R.attr.colorSurface))
                        .setSecondaryToolbarColor(context.getThemeColorOrThrow(com.google.android.material.R.attr.colorSurface))
                        .setNavigationBarColor(context.getThemeColorOrThrow(com.google.android.material.R.attr.colorSurface))
                        .build()
                    setDefaultColorSchemeParams(defaultParams)
                    setExitAnimations(
                        context,
                        R.anim.anim_fragment_pop_enter,
                        R.anim.anim_fragment_pop_exit
                    )
                }.build().intent.apply {
                    data = url.toUri()
                }
            }

            fun getSteamAppIntent(context: Context): Intent? {
                if (isAppInstalled(context, "com.valvesoftware.android.steam.community")) {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
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
    )
}

private val activityOptions = ActivityOptionsCompat.makeCustomAnimation(
    App.getInstance(),
    R.anim.anim_fragment_enter,
    R.anim.anim_fragment_exit
).toBundle()
