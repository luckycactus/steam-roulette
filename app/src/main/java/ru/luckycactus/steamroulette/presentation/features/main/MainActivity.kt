package ru.luckycactus.steamroulette.presentation.features.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_main_flow.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.*
import ru.luckycactus.steamroulette.di.core.ComponentOwner
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.presentation.features.game_details.GameDetailsFragment
import ru.luckycactus.steamroulette.presentation.features.login.LoginFragment
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
                is MainViewModel.Screen.GameDetails -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.container,
                            GameDetailsFragment.newInstance(it.game)
                        )
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit()
                }
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

    companion object {
        const val FRAGMENT_MAIN_FLOW_TAG = "FRAGMENT_MAIN_FLOW_TAG"
    }
}