package ru.luckycactus.steamroulette.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.lazyNonThreadSafe
import ru.luckycactus.steamroulette.presentation.login.LoginFragment
import ru.luckycactus.steamroulette.presentation.observe
import ru.luckycactus.steamroulette.presentation.roulette.RouletteFragment

class MainActivity : AppCompatActivity() {

    val viewModel: MainViewModel by lazyNonThreadSafe {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            viewModel.onColdStart()
        }

        observe(viewModel.screenLiveData) {
            it.handleIfNotHandled { screen ->
                when (screen) {
                    MainViewModel.Screen.Login ->
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, LoginFragment.newInstance())
                            .commit()
                    MainViewModel.Screen.Roulette ->
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, RouletteFragment.newInstance())
                            .commit()
                }
            }
        }
    }
}
