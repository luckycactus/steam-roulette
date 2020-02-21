package ru.luckycactus.steamroulette.presentation.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import ru.luckycactus.steamroulette.domain.games.entity.GameHeader
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsFragment
import ru.luckycactus.steamroulette.presentation.features.login.LoginFragment
import ru.luckycactus.steamroulette.presentation.features.roulette.RouletteFragment
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
        val enableSharedElementTransition: Boolean
    ) : Screens() {
        override fun getFragment(): Fragment =
            GameDetailsFragment.newInstance(game, enableSharedElementTransition)
    }

    data class ExternalSteamFlow(
        val url: String,
        val trySteamApp: Boolean
    ) : Screens() {
        override fun getActivityIntent(context: Context): Intent {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (trySteamApp && isAppInstalled(
                    context,
                    "com.valvesoftware.android.steam.community"
                )
            ) {
                with(intent) {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    `package` = "com.valvesoftware.android.steam.community"
                }
                if (intent.resolveActivity(context.packageManager) == null) {
                    with(intent) {
                        `package` = null
                        flags = 0
                    }
                }
            }
            return intent
        }
    }
}