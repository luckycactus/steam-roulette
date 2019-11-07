package ru.luckycactus.steamroulette.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.di.common.AppComponent
import ru.luckycactus.steamroulette.di.common.ComponentOwner
import ru.luckycactus.steamroulette.di.common.InjectionManager
import ru.luckycactus.steamroulette.di.common.component
import ru.luckycactus.steamroulette.presentation.login.LoginFragment
import ru.luckycactus.steamroulette.presentation.utils.observeEvent
import ru.luckycactus.steamroulette.presentation.utils.viewModel

class MainActivity : AppCompatActivity(), ComponentOwner<MainActivityComponent> {

    val viewModel by viewModel { component.mainViewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            viewModel.onColdStart()
        }

        observeEvent(viewModel.screen) { screen ->
            when (screen) {
                MainViewModel.Screen.Login ->
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, LoginFragment.newInstance())
                        .commit()
                MainViewModel.Screen.Roulette ->
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, MainFlowFragment.newInstance())
                        .commit()
            }
        }
    }

    override fun createComponent(): MainActivityComponent =
        InjectionManager.findComponent<AppComponent>()
            .mainActivityComponentFactory()
            .create(this)
}