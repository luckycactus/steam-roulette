package ru.luckycactus.steamroulette.presentation.features.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.commit
import kotlinx.android.synthetic.main.fragment_main_flow.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.AppComponent
import ru.luckycactus.steamroulette.di.common.component
import ru.luckycactus.steamroulette.di.core.ComponentOwner
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.domain.games.entity.OwnedGame
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsFragment
import ru.luckycactus.steamroulette.presentation.features.login.LoginFragment
import ru.luckycactus.steamroulette.presentation.features.roulette.RouletteFragment
import ru.luckycactus.steamroulette.presentation.utils.isAppInstalled
import ru.luckycactus.steamroulette.presentation.utils.observeEvent
import ru.luckycactus.steamroulette.presentation.utils.showSnackbar
import ru.luckycactus.steamroulette.presentation.utils.viewModel


class MainActivity : AppCompatActivity(),
    ComponentOwner<MainActivityComponent> {

    val viewModel by viewModel { component.mainViewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            viewModel.onColdStart()
        }

        observeEvent(viewModel.screen) {
            when (it) {
                MainViewModel.Screen.Login ->
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, LoginFragment.newInstance())
                        .commit()
                MainViewModel.Screen.Roulette ->
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.container,
                            MainFlowFragment.newInstance(),
                            FRAGMENT_MAIN_FLOW_TAG
                        )
                        .setReorderingAllowed(true)
                        .commit()
            }
        }

        observeEvent(viewModel.errorMessage) {
            container.showSnackbar(it) {
                anchorView = toolbar
            }
        }
    }

    override fun createComponent(): MainActivityComponent =
        InjectionManager.findComponent<AppComponent>()
            .mainActivityComponentFactory()
            .create(this)

    fun onGameClick(sharedViews: List<View>, game: OwnedGame) {
        supportFragmentManager.commit {
            hide(supportFragmentManager.findFragmentById(R.id.container)!!)
            add(
                R.id.container,
                GameDetailsFragment.newInstance(game, sharedViews.isNotEmpty())
            )
            for (sharedView in sharedViews) {
                addSharedElement(sharedView, ViewCompat.getTransitionName(sharedView)!!)
            }
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    fun openUrl(url: String, trySteamApp: Boolean) {
        //todo into navigation
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (trySteamApp && isAppInstalled(this, "com.valvesoftware.android.steam.community")) {
            with(intent) {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                `package` = "com.valvesoftware.android.steam.community"
            }
            if (intent.resolveActivity(packageManager) == null) {
                with(intent) {
                    `package` = null
                    flags = 0
                }
            }
        }
        startActivity(intent)
    }

    companion object {
        const val FRAGMENT_MAIN_FLOW_TAG = "FRAGMENT_MAIN_FLOW_TAG"
    }
}