package ru.luckycactus.steamroulette.presentation.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.github.terrakok.cicerone.androidx.ActivityScreen
import com.github.terrakok.cicerone.androidx.Creator
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.features.about.AboutFragment
import ru.luckycactus.steamroulette.presentation.features.about.AppLibrariesFragment
import ru.luckycactus.steamroulette.presentation.features.detailed_description.DetailedDescriptionFragment
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsFragment
import ru.luckycactus.steamroulette.presentation.features.games.LibraryFragment
import ru.luckycactus.steamroulette.presentation.features.games.OldLibraryFragment
import ru.luckycactus.steamroulette.presentation.features.imageview.ImageGalleryPagerFragment
import ru.luckycactus.steamroulette.presentation.features.login.LoginFragment
import ru.luckycactus.steamroulette.presentation.features.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.features.system_reqs.SystemReqsFragment
import ru.luckycactus.steamroulette.presentation.utils.customtabs.CustomTabsHelper
import ru.luckycactus.steamroulette.presentation.utils.extensions.getThemeColorOrThrow
import ru.luckycactus.steamroulette.presentation.utils.isAppInstalled

object Screens {

    fun Login() = FragmentScreen() {
        LoginFragment.newInstance()
    }

    fun Roulette() = FragmentScreen() {
        RouletteFragment.newInstance()
    }

    fun GameDetails(game: GameHeader, color: Int) = FragmentScreen() {
        GameDetailsFragment.newInstance(game, color)
    }

    fun SystemReqs(
        appName: String,
        systemReqs: List<SystemRequirements>
    ) = FragmentScreen {
        SystemReqsFragment.newInstance(appName, systemReqs)
    }

    fun DetailedDescription(
        appName: String,
        detailedDescription: String
    ) = FragmentScreen {
        DetailedDescriptionFragment.newInstance(appName, detailedDescription)
    }

    fun About() = FragmentScreen {
        AboutFragment.newInstance()
    }

    fun UsedLibraries() = FragmentScreen {
        AppLibrariesFragment.newInstance()
    }

    fun OldLibrary() = FragmentScreen {
        OldLibraryFragment.newInstance()
    }

    fun Library() = FragmentScreen {
        LibraryFragment.newInstance()
    }

    fun HiddenGames() = FragmentScreen {
        OldLibraryFragment.newInstance(true)
    }

    fun <T : Parcelable> ImageViewer(
        items: List<T>,
        index: Int,
        url: (T) -> String,
        thumbnail: (T) -> String?
    ) = FragmentScreen {
        ImageGalleryPagerFragment.newInstance(items, index, url, thumbnail)
    }

    fun ExternalBrowserFlow(
        url: String,
        trySteamApp: Boolean = false
    ) = object : ActivityScreen {

        override val startActivityOptions = createAnimationsOptions()

        override fun createIntent(context: Context): Intent {
            if (trySteamApp) {
                val intent = getSteamAppIntent(context)
                if (intent != null)
                    return intent
            }
            return if (CustomTabsHelper.isCustomTabsSupported(context)) {
                createCustomTabsIntent(context)
            } else {
                createDefaultIntent()
            }
        }

        private fun createCustomTabsIntent(context: Context): Intent {
            return CustomTabsIntent.Builder().apply {
                val defaultParams = CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(context.getThemeColorOrThrow(R.attr.colorSurface))
                    .setSecondaryToolbarColor(context.getThemeColorOrThrow(R.attr.colorSurface))
                    .setNavigationBarColor(context.getThemeColorOrThrow(R.attr.colorSurface))
                    .build()
                setDefaultColorSchemeParams(defaultParams)
                setExitAnimations(
                    context,
                    R.anim.anim_fragment_pop_enter,
                    R.anim.anim_fragment_pop_exit
                )
            }.build().intent.apply {
                data = Uri.parse(url)
            }
        }

        private fun getSteamAppIntent(context: Context): Intent? {
            if (isAppInstalled(context, "com.valvesoftware.android.steam.community")) {
                val intent = createDefaultIntent()
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

        private fun createDefaultIntent() = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }
}

private fun createAnimationsOptions(): Bundle? {
    return ActivityOptionsCompat.makeCustomAnimation(
        App.getInstance(),
        R.anim.anim_fragment_enter,
        R.anim.anim_fragment_exit
    ).toBundle()
}

// todo compose need only for "forward"
// override default clearContainer value to false
interface FragmentScreen : com.github.terrakok.cicerone.androidx.FragmentScreen {
//    override val clearContainer: Boolean get() = false

    companion object {
        operator fun invoke(
            key: String? = null,
            clearContainer: Boolean = false,
            fragmentCreator: Creator<FragmentFactory, Fragment>
        ) = object : FragmentScreen {
            override val screenKey = key ?: fragmentCreator::class.java.name
            override val clearContainer = clearContainer
            override fun createFragment(factory: FragmentFactory) = fragmentCreator.create(factory)
        }
    }
}